package de.mm20.launcher2.ui.medieval

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileInputStream

/*
 * T22: set the bundled orb picture as the LOCK SCREEN wallpaper only (never the home wallpaper), and put the old
 * one back. Needs only the normal permission SET_WALLPAPER. Before the first change the old lock wallpaper is copied
 * into the app's private files; if Android will not hand it over, Restore falls back to clearing our lock wallpaper.
 * UNVERIFIED on One UI 5: Samsung's handling of FLAG_LOCK. Every failure is logged with the tag medieval.
 */

private const val Tag = "medieval"
internal const val LockAsset = "lock/deliverance-orb-lock.jpg"
private const val BackupFile = "lock-backup.bin"
private const val BackupNone = "lock-backup.none"

internal data class LockResult(val ok: Boolean, val message: String)

private fun backupFile(context: Context) = File(context.filesDir, BackupFile)
private fun noneMarker(context: Context) = File(context.filesDir, BackupNone)

/** True when there is something to put back: a saved copy of the old picture, or a note that there was none. */
internal fun hasLockBackup(context: Context): Boolean = backupFile(context).exists() || noneMarker(context).exists()

/** Copies the current lock wallpaper once, before the first change. Returns false only if it could not be read. */
private fun backupOldLockWallpaper(context: Context, wm: WallpaperManager): Boolean {
    if (hasLockBackup(context)) return true
    try {
        val fd = wm.getWallpaperFile(WallpaperManager.FLAG_LOCK)
        if (fd == null) {
            // No separate lock wallpaper: the lock screen follows the home wallpaper. Restore will clear ours.
            noneMarker(context).writeText("none")
            Log.i(Tag, "Lock wallpaper: no separate lock wallpaper to back up")
            return true
        }
        fd.use { p ->
            FileInputStream(p.fileDescriptor).use { input ->
                backupFile(context).outputStream().use { input.copyTo(it) }
            }
        }
        Log.i(Tag, "Lock wallpaper: old picture saved (${backupFile(context).length()} bytes)")
        return true
    } catch (e: Exception) {
        Log.e(Tag, "Lock wallpaper: cannot read the old lock wallpaper", e)
        backupFile(context).delete()
        return false
    }
}

internal fun setOrbAsLockWallpaper(context: Context): LockResult {
    val wm = WallpaperManager.getInstance(context)
    if (!wm.isWallpaperSupported || !wm.isSetWallpaperAllowed) {
        Log.e(Tag, "Lock wallpaper: supported=${wm.isWallpaperSupported} allowed=${wm.isSetWallpaperAllowed}")
        return LockResult(false, "This phone does not allow apps to change the wallpaper.")
    }
    val savedOld = backupOldLockWallpaper(context, wm)
    try {
        val bitmap = context.assets.open(LockAsset).use { BitmapFactory.decodeStream(it) }
            ?: return LockResult(false, "The orb picture inside the app could not be read.").also {
                Log.e(Tag, "Lock wallpaper: decode of $LockAsset returned null")
            }
        val id = wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
        bitmap.recycle()
        if (id == 0) {
            Log.e(Tag, "Lock wallpaper: setBitmap returned 0 (failure)")
            return LockResult(false, "Android refused to change the lock screen. Nothing was changed.")
        }
        Log.i(Tag, "Lock wallpaper: set, id=$id, oldSaved=$savedOld")
        val extra = if (savedOld) "" else " (Your old picture could not be saved, so it cannot be put back from here.)"
        return LockResult(true, "Done. Lock the phone to see it.$extra")
    } catch (e: SecurityException) {
        Log.e(Tag, "Lock wallpaper: permission refused", e)
        return LockResult(false, "Android did not give this app the right to change the wallpaper.")
    } catch (e: Exception) {
        Log.e(Tag, "Lock wallpaper: set failed", e)
        return LockResult(false, "Could not set it: ${e.javaClass.simpleName}. Nothing was changed.")
    }
}

internal fun restoreLockWallpaper(context: Context): LockResult {
    val wm = WallpaperManager.getInstance(context)
    try {
        val file = backupFile(context)
        if (file.exists()) {
            file.inputStream().use { wm.setStream(it, null, true, WallpaperManager.FLAG_LOCK) }
        } else if (noneMarker(context).exists()) {
            wm.clear(WallpaperManager.FLAG_LOCK)
        } else {
            return LockResult(false, "There is no saved old picture.")
        }
        file.delete()
        noneMarker(context).delete()
        Log.i(Tag, "Lock wallpaper: old one put back")
        return LockResult(true, "Your old lock screen picture is back.")
    } catch (e: Exception) {
        Log.e(Tag, "Lock wallpaper: restore failed", e)
        return LockResult(false, "Could not put it back: ${e.javaClass.simpleName}. Choose a picture in Samsung's Wallpaper settings.")
    }
}
