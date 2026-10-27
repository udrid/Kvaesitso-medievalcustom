/* T9 preview helper: the orb with its Latin ring in four ring-text styles.
   Geometry copies Orb.kt: ring box 176, circles r86 / r57, text middle on r71.5, phrases centred at -90/30/150 deg,
   each spread over 106 deg, a mark in the middle of each 14 deg gap, orb 124 at (26,26). Drawn 1.0803x bigger. */
var T9 = (function () {
  var PHRASES = ['NOSCE TE IPSUM', 'SAPERE AUDE', 'LUX ET VERITAS'];
  var R = 71.5, SPAN = 106, C = 88;
  var G = '#c9a55a', GD = '#8c6d3a', GL = '#e3cf9c', RD = '#a8352b';
  var ctx = document.createElement('canvas').getContext('2d');

  function measure(font, size, ch) { ctx.font = font.replace('$', size + 'px'); return ctx.measureText(ch).width; }

  /* letters of one phrase: [{ch, rot, x, y, size}] (screen angle maths of drawSpreadArcText) */
  function layout(text, idx, font, size, initialScale) {
    var centerDeg = -90 + 120 * idx, lower = Math.sin(centerDeg * Math.PI / 180) > 0;
    var sizes, widths, gap, arc = SPAN * Math.PI / 180 * R, s = size;
    for (var tries = 0; tries < 40; tries++) {
      sizes = text.split('').map(function (c, i) { return i === 0 ? s * initialScale : s; });
      widths = text.split('').map(function (c, i) { return measure(font, sizes[i], c); });
      gap = (arc - widths.reduce(function (a, b) { return a + b; }, 0)) / (text.length - 1);
      if (gap >= 0.8) break;
      s -= 0.25;
    }
    var out = [], along = 0;
    text.split('').forEach(function (ch, i) {
      var mid = along + widths[i] / 2, off = mid / R * 180 / Math.PI;
      var deg = lower ? centerDeg + SPAN / 2 - off : centerDeg - SPAN / 2 + off;
      var cap = sizes[i] * 0.7;
      out.push({ ch: ch, rot: lower ? deg - 90 : deg + 90, y: lower ? C + R + cap / 2 : C - R + cap / 2, size: sizes[i], first: i === 0, space: ch === ' ' });
      along += widths[i] + gap;
    });
    return out;
  }

  function letters(L, attrs, extra) {
    return L.filter(function (l) { return !l.space; }).map(function (l) {
      var a = typeof attrs === 'function' ? attrs(l) : attrs;
      return '<text x="' + C + '" y="' + l.y.toFixed(2) + '" transform="rotate(' + l.rot.toFixed(2) + ' ' + C + ' ' + C + ')" font-size="' + l.size.toFixed(2) + '" ' + a + '>' + l.ch + '</text>';
    }).join('');
  }

  /* point on the band at screen angle deg, drawn in a frame whose x axis runs along the band */
  function at(deg, inner) { return '<g transform="translate(' + C + ' ' + C + ') rotate(' + deg + ') translate(' + R + ' 0) rotate(90)">' + inner + '</g>'; }

  function crossPath(s) { var w = s * .42; return 'M0 0L' + (-w) + ' ' + (-s) + 'L' + w + ' ' + (-s) + 'ZM0 0L' + (-w) + ' ' + s + 'L' + w + ' ' + s + 'ZM0 0L' + (-s) + ' ' + (-w) + 'L' + (-s) + ' ' + w + 'ZM0 0L' + s + ' ' + (-w) + 'L' + s + ' ' + w + 'Z'; }

  var STYLES = {
    built: function (id) {
      var f = 'Cinzel,serif', fw = '700 $ Cinzel';
      var s = '';
      PHRASES.forEach(function (p, i) { s += letters(layout(p, i, fw, 13, 1), 'font-family="Cinzel" font-weight="700" text-anchor="middle" fill="' + GD + '"'); });
      for (var i = 0; i < 3; i++) s += at(-30 + 120 * i, '<path d="' + crossPath(3.6) + '" fill="' + GD + '"/>');
      return { defs: '', body: s };
    },
    illum: function (id) {
      var fw = '700 $ Cinzel', s = '';
      var defs = '<linearGradient id="' + id + 'g" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#ecd9a0"/><stop offset=".55" stop-color="#c9a55a"/><stop offset="1" stop-color="#9c7a36"/></linearGradient>' +
        '<linearGradient id="' + id + 'r" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#d9574a"/><stop offset="1" stop-color="#8a2a20"/></linearGradient>' +
        '<filter id="' + id + 'f" x="-20%" y="-20%" width="140%" height="140%"><feGaussianBlur in="SourceAlpha" stdDeviation="1.6" result="b"/><feFlood flood-color="#e5a64a" flood-opacity=".42"/><feComposite in2="b" operator="in" result="gl"/><feMerge><feMergeNode in="gl"/><feMergeNode in="SourceGraphic"/></feMerge></filter>';
      var inner = '';
      PHRASES.forEach(function (p, i) {
        inner += letters(layout(p, i, fw, 12.4, 1.22), function (l) {
          return l.first ? 'font-family="Cinzel" font-weight="700" text-anchor="middle" fill="url(#' + id + 'r)" stroke="' + G + '" stroke-width=".55" paint-order="stroke"'
            : 'font-family="Cinzel" font-weight="700" text-anchor="middle" fill="url(#' + id + 'g)" stroke="#140b04" stroke-width=".55" paint-order="stroke"';
        });
      });
      s += '<g filter="url(#' + id + 'f)">' + inner + '</g>';
      for (var i = 0; i < 3; i++) s += at(-30 + 120 * i,
        '<path d="M0 -3.4L2.6 0L0 3.4L-2.6 0Z" fill="' + RD + '" stroke="' + G + '" stroke-width=".7"/>' +
        '<circle cx="-6.2" r="1" fill="' + G + '"/><circle cx="6.2" r="1" fill="' + G + '"/>' +
        '<path d="M-4.4 0H-3.4M3.4 0H4.4" stroke="' + GD + '" stroke-width=".6"/>');
      return { defs: defs, body: s };
    },
    engraved: function (id) {
      var fw = '700 $ Cinzel', s = '', base = 'font-family="Cinzel" font-weight="700" text-anchor="middle"';
      var defs = '<linearGradient id="' + id + 'm" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#4a3a1c"/><stop offset="1" stop-color="#7a6030"/></linearGradient>';
      var Ls = PHRASES.map(function (p, i) { return layout(p, i, fw, 13, 1); });
      /* light edge below, dark lip above, darker metal in the cut: reads as cut into a bronze band */
      function pass(dy, fill, op) { return '<g transform="translate(0 ' + dy + ')" opacity="' + op + '">' + Ls.map(function (L) { return letters(L, base + ' fill="' + fill + '"'); }).join('') + '</g>'; }
      s += pass(.65, GL, .5) + pass(-.45, '#000', .85) + pass(0, 'url(#' + id + 'm)', 1);
      /* inner shadow: thin dark stroke inside the cut at the top */
      s += Ls.map(function (L) { return letters(L, base + ' fill="none" stroke="#000" stroke-opacity=".45" stroke-width=".5"'); }).join('');
      for (var i = 0; i < 3; i++) s += at(-30 + 120 * i,
        '<g transform="translate(0 .65)" opacity=".5"><path d="' + crossPath(3.6) + '" fill="' + GL + '"/></g><g transform="translate(0 -.45)"><path d="' + crossPath(3.6) + '" fill="#000"/></g><path d="' + crossPath(3.6) + '" fill="url(#' + id + 'm)"/>');
      return { defs: defs, body: s };
    },
    blackLower: function (id) { return STYLES.black(id, true); },
    black: function (id, lower) {
      var fw = '$ UnifrakturMaguntia', s = '';
      var defs = '<linearGradient id="' + id + 'g" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#e3cf9c"/><stop offset=".6" stop-color="#c9a55a"/><stop offset="1" stop-color="#a98a46"/></linearGradient>' +
        '<filter id="' + id + 'f" x="-20%" y="-20%" width="140%" height="140%"><feGaussianBlur in="SourceAlpha" stdDeviation="1.2" result="b"/><feFlood flood-color="#e5a64a" flood-opacity=".3"/><feComposite in2="b" operator="in" result="gl"/><feMerge><feMergeNode in="gl"/><feMergeNode in="SourceGraphic"/></feMerge></filter>';
      var inner = '';
      PHRASES.forEach(function (p, i) { if (lower) p = p.charAt(0) + p.slice(1).toLowerCase().replace(/ (.)/g, function (m, c) { return ' ' + c.toUpperCase(); }); inner += letters(layout(p, i, fw, lower ? 16 : 13.5, 1), 'font-family="UnifrakturMaguntia" text-anchor="middle" fill="url(#' + id + 'g)" stroke="#140b04" stroke-width=".35" paint-order="stroke"'); });
      s += '<g filter="url(#' + id + 'f)">' + inner + '</g>';
      var curl = '<path d="M-3.4 0C-5 -3 -8.6 -3 -9 -.2C-9.2 1.6 -7.2 2.2 -6.6 .8" stroke="' + G + '" stroke-width=".8" fill="none" stroke-linecap="round"/><circle cx="-9.4" cy="-.6" r=".8" fill="' + G + '"/>';
      for (var i = 0; i < 3; i++) s += at(-30 + 120 * i,
        '<path d="' + crossPath(3.2) + '" fill="' + G + '"/>' + curl + '<g transform="scale(-1 1)">' + curl + '</g>');
      return { defs: defs, body: s };
    }
  };

  /* orb + ring. 176 box scaled 1.0803 (the real Home draws the whole body 8% larger). cls hooks are used by T9-vibe. */
  function orb(style, id) {
    var st = STYLES[style](id);
    var ring = '<svg class="ringsvg" width="176" height="176" viewBox="0 0 176 176" fill="none" style="position:absolute;left:0;top:0"><defs>' + st.defs + '</defs>' +
      '<circle cx="88" cy="88" r="86" stroke="#6b5430" stroke-width="1.5"/><circle cx="88" cy="88" r="57" stroke="#3a2e18"/>' + st.body + '</svg>';
    var core = '<div class="orbcore" style="position:absolute;left:26px;top:26px;width:124px;height:124px;border-radius:50%;border:2px solid #8c6d3a;background:radial-gradient(circle at 50% 38%,#4a2d14,#0f0904 72%);box-shadow:0 0 0 3px #0a0907,0 0 34px rgba(229,138,60,.26),inset 0 0 30px rgba(0,0,0,.85);display:flex;align-items:center;justify-content:center">' +
      '<svg class="flame" width="54" height="54" viewBox="0 0 24 24" fill="none" stroke="#f0a050" stroke-width="1.4" stroke-linejoin="round" style="filter:drop-shadow(0 0 6px rgba(229,138,60,.65));transform-origin:50% 90%"><path d="M12 3c1 3 5 5 5 10a5 5 0 0 1-10 0c0-2 1-3 2-4 0 2 1 3 2 3 0-3-1-5 1-9z"/></svg></div>';
    return '<div class="orbbox" style="position:relative;width:190px;height:190px"><div class="orbin" style="position:absolute;left:0;top:0;width:176px;height:176px;transform:scale(1.0803);transform-origin:0 0">' + ring + core + '</div></div>';
  }

  return { orb: orb, ready: function (cb) { Promise.all(['700 13px Cinzel', '14px UnifrakturMaguntia', '13px "IM Fell English"'].map(function (f) { return document.fonts.load(f); })).then(cb, cb); } };
})();
