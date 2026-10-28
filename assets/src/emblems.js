/* Emblems for the icon pack (24-unit line art, same 1.6 stroke as the T7 emblems) and the plates.
   Needs lib.js (G, RD, svg, E). Author: Claude for the Deliverance project. */
RD='#8a2a20';
var F='fill="'+G+'" stroke="none"';
var MORE={
 contacts:svg('<circle cx="12" cy="8.5" r="3.6"/><path d="M4.5 20c0-4.5 3.4-7 7.5-7s7.5 2.5 7.5 7z"/>'),
 files:svg('<path d="M3.5 10h17v9.5h-17z"/><path d="M3.5 10c0-3.5 3.8-6 8.5-6s8.5 2.5 8.5 6"/><path d="M3.5 14h17"/><rect x="10.5" y="12" width="3" height="4" fill="'+G+'"/>'),
 calc:svg('<rect x="3.5" y="4.5" width="17" height="15" rx=".5"/><path d="M6 8.5h12M6 12h12M6 15.5h12"/><circle cx="9" cy="8.5" r="1.3" '+F+'/><circle cx="14" cy="12" r="1.3" '+F+'/><circle cx="8" cy="15.5" r="1.3" '+F+'/>'),
 notes:svg('<rect x="4.5" y="3.5" width="12" height="17" rx=".5"/><path d="M7.5 8h6M7.5 11.5h6M7.5 15h4"/><path d="M19.5 5l-5 11-.9 3 3-1.2 4.2-10z"/>'),
 weather:svg('<circle cx="9" cy="9" r="3.4"/><path d="M9 2.4V4M2.4 9H4M4.4 4.4l1.1 1.1M13.6 4.4l-1.1 1.1"/><path d="M8 20h10a3.5 3.5 0 0 0 .5-6.95A5 5 0 0 0 9 14.2 3 3 0 0 0 8 20z"/>'),
 store:svg('<path d="M12 3.5v16M7 20h10M4 7h16"/><path d="M4 7l-2.5 6a3 3 0 0 0 5 0zM20 7l-2.5 6a3 3 0 0 0 5 0z"/>'),
 wallet:svg('<path d="M8 5.5c1.5 1.5 6.5 1.5 8 0l-1.5-2.5h-5z"/><path d="M8 5.5C4 9 3 12 3 15c0 3.5 3.5 5.5 9 5.5s9-2 9-5.5c0-3-1-6-5-9.5"/><path d="M12 10v6M10.2 12h2.8a1.4 1.4 0 0 1 0 2.8h-2.8"/>'),
 torch:svg('<path d="M9 3.5h6l1 5H8z"/><path d="M8 8.5l1.5 4v8h5v-8L16 8.5"/><path d="M12 12v3"/>'),
 health:svg('<path d="M12 20S4 15 4 9.5A4 4 0 0 1 12 8a4 4 0 0 1 8 1.5C20 15 12 20 12 20z" fill="'+RD+'"/>'),
 recorder:svg('<path d="M7 9.5a5 5 0 0 1 10 0c0 3-2.5 3.7-3.2 5.5-.6 1.6-.4 3.5-2.3 3.5-1.5 0-2.2-1-2.2-2.2"/><path d="M10 9.5a2 2 0 0 1 4 0c0 1.2-1 1.7-1.4 2.6"/>'),
 spyglass:svg('<path d="M4 15.5l9-4 1.5 3.5-9 4z"/><path d="M13 11.5L19.5 8.5 21 12l-6.5 3z"/><path d="M8 20.5l2.5-1"/>'),
 basket:svg('<path d="M3.5 10h17l-2 9.5h-13z"/><path d="M8 10l3-6M16 10l-3-6"/><path d="M9 13v3.5M12 13v3.5M15 13v3.5"/>'),
 wagon:svg('<path d="M3 6h14v8H3z"/><path d="M17 9h4l-1 5h-3"/><circle cx="7" cy="17.5" r="2.5"/><circle cx="16.5" cy="17.5" r="2.5"/>'),
 loaf:svg('<path d="M4 12c0-4 3-6.5 8-6.5s8 2.5 8 6.5c0 1.2-.6 1.8-1.5 2V18h-13v-4C4.6 13.8 4 13.2 4 12z"/><path d="M9 8.5l1 3M13 8l1 3.5"/>'),
 coins:svg('<ellipse cx="12" cy="7" rx="7" ry="3"/><path d="M5 7v5c0 1.7 3.1 3 7 3s7-1.3 7-3V7"/><path d="M5 12v5c0 1.7 3.1 3 7 3s7-1.3 7-3v-5"/>'),
 proclamation:svg('<path d="M5 3.5h12.5v14H5z"/><path d="M5 17.5c0 2 1.5 3 3 3h12V8h-2.5"/><path d="M8 7.5h6.5M8 10.5h6.5M8 13.5h4"/>'),
 banner:svg('<path d="M6 3v18"/><path d="M6 4.5h13l-3 4.5 3 4.5H6"/><circle cx="9.5" cy="9" r="1.2" '+F+'/>'),
 tavern:svg('<path d="M4 3v18M4 5h14"/><path d="M8 5v2M16 5v2"/><rect x="6.5" y="7" width="11" height="9" rx="1"/><path d="M9.5 10.5h5M9.5 13h3"/>'),
 dice:svg('<rect x="4.5" y="4.5" width="15" height="15" rx="2"/><circle cx="9" cy="9" r="1.2" '+F+'/><circle cx="15" cy="9" r="1.2" '+F+'/><circle cx="12" cy="12" r="1.2" '+F+'/><circle cx="9" cy="15" r="1.2" '+F+'/><circle cx="15" cy="15" r="1.2" '+F+'/>'),
 padlock:svg('<rect x="5" y="10.5" width="14" height="10" rx="1"/><path d="M8 10.5V7.5a4 4 0 0 1 8 0v3"/><circle cx="12" cy="15" r="1.3" '+F+'/><path d="M12 15.5V18"/>'),
 anvil:svg('<path d="M3 8h14.5c0 3-2 4-4 4.5V15l2.5 2.5V20H6v-2.5L8.5 15v-2.5C5.5 12 3.5 10.5 3 8z"/><path d="M17.5 8c1.5 0 3-.8 3.5-2.5"/>'),
 tower:svg('<path d="M6 20V9h12v11z"/><path d="M5 9V5h2.5v2h2V5h5v2h2V5H19v4z"/><path d="M10.5 20v-5a1.5 1.5 0 0 1 3 0v5"/>'),
 book:svg('<path d="M12 6c-2-1.5-5-2-8-1.5V18c3-.5 6 0 8 1.5 2-1.5 5-2 8-1.5V4.5C17 4 14 4.5 12 6z"/><path d="M12 6v13.5"/>'),
 dove:svg('<path d="M3 14c3.5 1 6-1 7-3.5l1.5-4c.8-1.5 3-1.5 3.8 0l.7 1.7 3.5-.2-1.2 2c0 5-3.5 9-9 9H5.5l2.5-3z"/><circle cx="15.2" cy="8.2" r=".7" '+F+'/>'),
 mask:svg('<path d="M4 5c5 1.5 11 1.5 16 0 .8 7-1 13-8 15-7-2-8.8-8-8-15z"/><path d="M7.5 10c1-.8 2.5-.8 3.2.2M13.3 10.2c.8-1 2.3-1 3.2-.2"/><path d="M9 15c2 1.5 4 1.5 6 0"/>'),
 brush:svg('<path d="M19.5 4C14 6 11 9.5 9.5 13.5l2 2C15.5 14 19 11 20.5 5.5z"/><path d="M9.5 13.5c-3 0-4 2-4 4 0 1-.8 1.8-1.8 2.3 3 .6 7-.8 7.8-4.3"/>'),
 lantern:svg('<path d="M9 3.5h6M12 3.5V2M8.5 5.5h7l1.5 2v9l-1.5 2h-7L7 16.5v-9z"/><path d="M12 9.5c1.5 1.5 1.5 3.5 0 5-1.5-1.5-1.5-3.5 0-5z" fill="'+RD+'"/><path d="M9 20.5h6"/>'),
 orb:svg('<circle cx="12" cy="11.5" r="7"/><path d="M8 20.5h8"/><path d="M12 8l.9 2.1 2.1.9-2.1.9-.9 2.1-.9-2.1L9 11l2.1-.9z" '+F+'/>')
};
RD='#a8352b';
var EMB={};
for(var k in E)EMB[k]=E[k][2];
for(var k in MORE)EMB[k]=MORE[k];
var EMBLEMS=Object.keys(EMB);
/* plates, drawn in a 192x192 cell */
var PLATE_HEATER='<svg x="18" y="6" width="155" height="180" viewBox="0 0 62 72"><path d="M31 2L58 12V38C58 55 44 66 31 70C18 66 4 55 4 38V12Z" fill="#15120d"/><path d="M4 12L31 2L58 12V22H4Z" fill="#3a1612"/><path d="M31 2L58 12V38C58 55 44 66 31 70C18 66 4 55 4 38V12Z" fill="none" stroke="#c9a55a" stroke-width="2"/><path d="M31 5L55 13.5V38C55 53 43 63 31 67C19 63 7 53 7 38V13.5Z" fill="none" stroke="#4a3a20" stroke-width=".8"/></svg>';
var PLATE_SEAL='<svg x="18" y="6" width="155" height="180" viewBox="0 0 62 72"><circle cx="31" cy="36" r="29" fill="#15120d" stroke="#c9a55a" stroke-width="2.5"/><circle cx="31" cy="36" r="24.5" fill="none" stroke="#4a3a20" stroke-width="1"/><path d="M7.5 24A25 25 0 0 1 54.5 24Z" fill="#3a1612" opacity=".9" transform="translate(0 -2)"/></svg>';
function cellSvg(name,flavor){
  var g=EMB[name], x, y, s;
  if(flavor==='bare'){x=36;y=36;s=120}else if(flavor==='seal'){x=63.5;y=63.5;s=65}else{x=63.5;y=61;s=65}
  g=g.replace('width="26" height="26"','x="'+x+'" y="'+y+'" width="'+s+'" height="'+s+'"');
  var plate=flavor==='heater'?PLATE_HEATER:flavor==='seal'?PLATE_SEAL:'';
  return '<svg width="192" height="192" viewBox="0 0 192 192">'+plate+g+'</svg>';
}
