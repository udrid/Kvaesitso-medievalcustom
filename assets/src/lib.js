/* S3 art library: palette, planetary medallions (from T4), emblems (from T7), wallpaper pieces. Author: Claude for the Deliverance project. */
var G='#c9a55a', GD='#8c6d3a', GL='#e3cf9c', RD='#a8352b';
var NS='http://www.w3.org/2000/svg';

/* alchemical signs, centred on 0,0, about 26 units tall */
var SIGN={
 sol:'<circle r="9.5"/><circle r="2" fill="'+G+'"/>',
 luna:'<path d="M5 -12A12 12 0 1 0 5 12A9 9 0 1 1 5 -12Z" fill="'+G+'" fill-opacity=".85"/>',
 mars:'<circle cx="-2.5" cy="3.5" r="7.5"/><path d="M3 -2L11 -10M4 -10H11V-3"/>',
 mercury:'<circle cy="2" r="5.2"/><path d="M0 7.2V14M-3.4 10.8H3.4M-6.5 -11A6.5 6.5 0 0 0 6.5 -11"/>',
 jupiter:'<path d="M-9 -6C-9 -13 -1 -13 -1 -6C-1 -1 -6 3 -11 4H10M4.5 -12V13"/>',
 venus:'<circle cy="-4.5" r="6.5"/><path d="M0 2V13M-4.5 7.5H4.5"/>',
 saturn:'<path d="M-6 -11H0M-3 -14V3M-3 -4C0 -7 6 -5 6 0C6 5 2 7 2 12"/>'
};

/* rings around the medallion, centre 44,35 in an 88x70 box; medallion radius 25 */
function rays(n,r1,r2,r3,fill){var s='';for(var i=0;i<n;i++){var a=i*2*Math.PI/n,b=a+Math.PI/n*0.55,c=a-Math.PI/n*0.55,l=(i%2?r2:r3);
 s+='<path d="M'+(r1*Math.cos(b)).toFixed(1)+' '+(r1*Math.sin(b)).toFixed(1)+'L'+(l*Math.cos(a)).toFixed(1)+' '+(l*Math.sin(a)).toFixed(1)+'L'+(r1*Math.cos(c)).toFixed(1)+' '+(r1*Math.sin(c)).toFixed(1)+'Z" fill="'+fill+'"/>';}return s}
function star4(x,y,r){return '<path d="M'+x+' '+(y-r)+'Q'+x+' '+y+' '+(x+r)+' '+y+'Q'+x+' '+y+' '+x+' '+(y+r)+'Q'+x+' '+y+' '+(x-r)+' '+y+'Q'+x+' '+y+' '+x+' '+(y-r)+'Z" fill="'+G+'"/>'}
function feathers(sd){var s='';for(var i=0;i<4;i++){var y=-6+i*5,x0=26,x1=40-i*2.2,y1=y-9+i*3.2;
 s+='<path d="M'+(sd*x0)+' '+y+'Q'+(sd*(x0+8))+' '+(y-8)+' '+(sd*x1)+' '+y1+'Q'+(sd*(x1-2))+' '+(y1+8)+' '+(sd*x0)+' '+(y+3)+'Z" fill="#2a2216" stroke="'+G+'" stroke-width="1"/>';}return s}
var RING={
 sol:function(){return rays(32,26,31,35,GD)+'<circle r="25" fill="none" stroke="'+G+'" stroke-width="1.2" stroke-dasharray="2 2.4"/>'},
 luna:function(){var s='<circle r="27" fill="none" stroke="'+GD+'" stroke-width="1" stroke-dasharray="1 3"/>';
  [[-36,-20,3],[36,-18,3.6],[-34,18,2.4],[37,17,2.8],[0,-33,2.6],[0,33,2.6]].forEach(function(p){s+=star4(p[0],p[1],p[2])});
  return s+'<path d="M-30 -5A5 5 0 1 0 -30 5A3.8 3.8 0 1 1 -30 -5Z" fill="'+G+'"/><path d="M30 -5A5 5 0 1 1 30 5A3.8 3.8 0 1 0 30 -5Z" fill="'+G+'"/>'},
 mars:function(){var s='';for(var i=0;i<8;i++){var a=i*Math.PI/4;s+='<g transform="rotate('+(i*45)+')"><path d="M26 0L35 '+(i%2?0:0)+'" stroke="'+GD+'" stroke-width="1"/>'+(i%2?'<path d="M27 -3.5L36 0L27 3.5Z" fill="'+GD+'"/>':'<path d="M27 -4.5L40 0L27 4.5L30 0Z" fill="'+RD+'" stroke="'+G+'" stroke-width=".9"/>')+'</g>'}
  return s+'<circle r="25" fill="none" stroke="'+G+'" stroke-width="1.2"/>'},
 mercury:function(){return feathers(1)+feathers(-1)+'<circle r="26" fill="none" stroke="'+GD+'" stroke-width="1"/>'},
 jupiter:function(){var s='';[45,135,225,315].forEach(function(d){s+='<g transform="rotate('+d+')"><path d="M24 -2L30 -5L29 -1L36 -3L28 4L29 0L24 2Z" fill="'+G+'"/></g>'});
  [0,90,180,270].forEach(function(d){s+='<g transform="rotate('+d+')"><circle cx="30" r="1.8" fill="'+RD+'" stroke="'+G+'" stroke-width=".7"/></g>'});
  return s+'<circle r="25" fill="none" stroke="'+G+'" stroke-width="1.2"/><circle r="28" fill="none" stroke="'+GD+'" stroke-width=".8" stroke-dasharray="1 2.6"/>'},
 venus:function(){var s='';for(var i=0;i<10;i++){s+='<g transform="rotate('+(i*36)+')"><path d="M25 0C28 -6 34 -6 36 0C34 6 28 6 25 0Z" fill="'+(i%2?'#2a2216':'#5a1a14')+'" stroke="'+G+'" stroke-width=".9"/></g>'}
  return s+'<circle r="25" fill="none" stroke="'+G+'" stroke-width="1.2"/>'},
 saturn:function(){var s='<path d="M0 -34L10 -28L28 -22L34 0L28 22L10 28L0 34L-10 28L-28 22L-34 0L-28 -22L-10 -28Z" fill="none" stroke="'+GD+'" stroke-width="1"/>';
  for(var i=0;i<12;i++){s+='<g transform="rotate('+(i*30)+')"><path d="M26 0H31" stroke="'+G+'" stroke-width="1.4"/></g>'}
  [0,90,180,270].forEach(function(d){s+='<g transform="rotate('+d+' 0 0)"><path d="M31 -3L36 0L31 3L26 0Z" fill="'+RD+'" stroke="'+G+'" stroke-width=".8" transform="translate(4 0)"/></g>'});
  return s+'<circle r="25" fill="none" stroke="'+G+'" stroke-width="1.2"/>'}
};
RD='#8a2a20';
var SHIELD='<svg class="shield" viewBox="0 0 62 72" aria-hidden="true"><path d="M31 2L58 12V38C58 55 44 66 31 70C18 66 4 55 4 38V12Z" fill="#15120d"/><path d="M4 12L31 2L58 12V22H4Z" fill="#3a1612"/><path d="M31 2L58 12V38C58 55 44 66 31 70C18 66 4 55 4 38V12Z" fill="none" stroke="#8c6d3a" stroke-width="2"/></svg>';
function svg(inner){return '<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="'+G+'" stroke-width="1.6" stroke-linejoin="round" stroke-linecap="round" aria-hidden="true">'+inner+'</svg>';}
var E={
 x:['X','raven',svg('<path d="M3 14c3-1 5-3 6-6 2 1 4 1 6 0l5 3-4 1c0 4-3 7-8 7H6l2-3-5-2z"/><path d="M10 13c2 0 4 1 5 3"/><circle cx="14.2" cy="9.6" r=".7" fill="'+G+'" stroke="none"/>')],
 discord:['Discord','goblet',svg('<path d="M6.5 4h11c0 5.5-1.5 8.5-5.5 9.5C8 12.5 6.5 9.5 6.5 4z"/><path d="M6.8 7.5h10.4"/><path d="M12 13.5V19"/><path d="M8 20.5h8"/>')],
 youtube:['YouTube','scroll',svg('<rect x="6" y="6.5" width="12" height="11" rx=".5"/><rect x="3.6" y="4.8" width="2.8" height="14.4" rx="1.4"/><rect x="17.6" y="4.8" width="2.8" height="14.4" rx="1.4"/><path d="M10.6 9.6l4 2.4-4 2.4z" fill="'+G+'"/>')],
 messages:['Messages','sealed letter',svg('<path d="M3.5 6h17v12h-17z"/><path d="M3.5 6.5l8.5 6.2 8.5-6.2"/><circle cx="12" cy="13" r="2.6" fill="'+RD+'"/>')],
 maps:['Maps','compass',svg('<circle cx="12" cy="12" r="9"/><path d="M12 5.2l1.8 5 5 1.8-5 1.8-1.8 5-1.8-5-5-1.8 5-1.8z"/><path d="M12 1.6v2.2"/>')],
 phone:['Phone','bell',svg('<path d="M12 4c-3 0-5 2.5-5 6v4l-2 3h14l-2-3v-4c0-3.5-2-6-5-6z"/><path d="M10 19a2 2 0 0 0 4 0"/><path d="M12 2.4V4"/>')],
 camera:['Camera','watching eye',svg('<path d="M2.5 12C5 7.6 8.5 6 12 6s7 1.6 9.5 6c-2.5 4.4-6 6-9.5 6s-7-1.6-9.5-6z"/><circle cx="12" cy="12" r="3"/><circle cx="12" cy="12" r=".9" fill="'+G+'" stroke="none"/>')],
 browser:['Browser','orb and cross',svg('<circle cx="12" cy="14.5" r="7"/><path d="M5 14.5h14"/><ellipse cx="12" cy="14.5" rx="3.2" ry="7"/><path d="M12 1.8v4M10.2 3.6h3.6"/>')],
 gallery:['Gallery','framed portrait',svg('<path d="M3.5 4.5h17v15h-17z"/><path d="M6 7h12v10H6z"/><path d="M6 17l4.2-5 3 3.4 2-2L18 17"/><circle cx="15.4" cy="9.8" r="1.1"/>')],
 settings:['Settings','ship\'s wheel',svg('<circle cx="12" cy="12" r="6.8"/><circle cx="12" cy="12" r="2"/><path d="M12 1.8v8.2M12 14v8.2M1.8 12h8.2M14 12h8.2"/><path d="M13.4 10.6l5.8-5.8M10.6 10.6L4.8 4.8M13.4 13.4l5.8 5.8M10.6 13.4l-5.8 5.8"/>')],
 clock:['Clock','hourglass',svg('<path d="M6.5 3h11M6.5 21h11"/><path d="M8 3v3.5l4 5.5-4 5.5V21M16 3v3.5l-4 5.5 4 5.5V21"/><path d="M9.8 19.2h4.4L12 16z" fill="'+G+'"/>')],
 calendar:['Calendar','book of days',svg('<path d="M5 4.5a2 2 0 0 1 2-2h11.5v16.8H7a2 2 0 0 0-2 2z"/><path d="M5 19.3a2 2 0 0 1 2-2h11.5"/><path d="M14.5 2.5v6l1.6-1.3 1.6 1.3v-6" fill="'+RD+'"/>')],
 spotify:['Spotify','lute',svg('<ellipse cx="8.6" cy="15.4" rx="5.2" ry="5.4" transform="rotate(-40 8.6 15.4)"/><circle cx="8.6" cy="15.4" r="1.5"/><path d="M12.3 11.7L19 5"/><path d="M17.6 3.6l2.8 2.8"/>')],
 whatsapp:['WhatsApp','herald\'s trumpet',svg('<path d="M3 10.8h11.5M3 13.2h11.5"/><path d="M14.5 10.8L21 7.2v9.6l-6.5-3.6"/><path d="M3 10.2v3.6"/><path d="M7.5 13.2v6l2-1.3 2 1.3v-6" fill="'+RD+'"/>')],
 gmail:['Gmail','quill',svg('<path d="M20 4C12 4.2 7 9 6.4 16.5L4.2 20l3.4-2.2C15 17.2 19.800 12.200 20 4z"/><path d="M7 17.2L15.200 8.800"/>')]
};
RD='#a8352b';
var DAYS=[['sol','Sunday','Sol'],['luna','Monday','Luna'],['mars','Tuesday','Mars'],['mercury','Wednesday','Mercury'],['jupiter','Thursday','Jupiter'],['venus','Friday','Venus'],['saturn','Saturday','Saturn']];
function medal(day,cx,cy,s,ring){
  return '<g transform="translate('+cx+' '+cy+') scale('+s+')">'+(ring===false?'':RING[day]())+'<circle r="25" fill="#15120d" stroke="'+G+'" stroke-width="1.4"/><circle r="22" fill="none" stroke="'+GD+'" stroke-width=".6"/><g fill="none" stroke="'+G+'" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">'+SIGN[day]+'</g></g>';
}
function ground(){
  return '<defs><radialGradient id="bg" cx=".5" cy=".38" r=".75"><stop offset="0" stop-color="#26211a"/><stop offset=".55" stop-color="#14110d"/><stop offset="1" stop-color="#0a0907"/></radialGradient><radialGradient id="vg" cx=".5" cy=".5" r=".75"><stop offset=".55" stop-color="#000" stop-opacity="0"/><stop offset="1" stop-color="#000" stop-opacity=".72"/></radialGradient></defs>'+
  '<rect width="1080" height="2400" fill="url(#bg)"/>';
}
function finish(){
  return '<rect width="1080" height="2400" filter="url(#grain)" opacity=".5"/><rect width="1080" height="2400" fill="url(#vg)"/>';
}
function ringText(cx,cy,r,size,op,id){
  var txt='NOSCE TE IPSUM + SAPERE AUDE + LUX ET VERITAS + ';
  return '<defs><path id="'+id+'" d="M'+(cx-r)+' '+cy+'A'+r+' '+r+' 0 1 1 '+(cx+r)+' '+cy+'A'+r+' '+r+' 0 1 1 '+(cx-r)+' '+cy+'"/></defs><text font-family="Cinzel" font-weight="700" font-size="'+size+'" fill="'+G+'" opacity="'+op+'"><textPath href="#'+id+'" textLength="'+(2*Math.PI*r-8).toFixed(0)+'" lengthAdjust="spacing">'+txt+'</textPath></text>';
}
function rnd(seed){return function(){seed=(seed*16807)%2147483647;return (seed-1)/2147483646}}
function wpA(){
  var s=ground(),cx=540,cy=1780;
  s+='<g fill="none" stroke="'+G+'" opacity=".3">';
  [300,330,420,436].forEach(function(r,i){s+='<circle cx="'+cx+'" cy="'+cy+'" r="'+r+'" stroke-width="'+(i==2?1.5:3)+'"/>'});
  for(var i=0;i<72;i++){var a=i*Math.PI/36,l=i%6?12:26;s+='<path d="M'+(cx+Math.cos(a)*300).toFixed(1)+' '+(cy+Math.sin(a)*300).toFixed(1)+'L'+(cx+Math.cos(a)*(300-l)).toFixed(1)+' '+(cy+Math.sin(a)*(300-l)).toFixed(1)+'" stroke-width="2"/>'}
  s+='<path d="M'+cx+' '+(cy-180)+'L'+(cx+40)+' '+cy+'L'+cx+' '+(cy+180)+'L'+(cx-40)+' '+cy+'Z M'+(cx-180)+' '+cy+'L'+cx+' '+(cy+40)+'L'+(cx+180)+' '+cy+'L'+cx+' '+(cy-40)+'Z" stroke-width="2.5"/></g>';
  s+=ringText(cx,cy,372,30,.38,'ra');
  return s+finish();
}
function wpB(day,nm,pl){
  day=day||'sol';nm=nm||'Sunday';pl=pl||'SOL';
  var s=ground();
  s+=medal(day,540,1560,7);
  s+='<text x="540" y="2000" text-anchor="middle" font-family="UnifrakturMaguntia" font-size="150" fill="#ecdfbf" opacity=".92">'+nm+'</text>';
  s+='<text x="540" y="2078" text-anchor="middle" font-family="Cinzel" font-weight="700" font-size="34" letter-spacing="16" fill="'+GD+'">'+pl+'</text>';
  s+='<path d="M300 2118H780" stroke="'+GD+'" stroke-width="2"/><path d="M540 2106l12 12-12 12-12-12z" fill="'+G+'"/>';
  return s+finish();
}
function wpC(sc,cyy){
  var s=ground(),cx=540,cy=1560,R=250,r=rnd(11);
  if(sc)s+="<g transform=\"translate(540 "+cyy+") scale("+sc+") translate(-540 -1560)\">";
  s+='<defs><radialGradient id="ob" cx=".4" cy=".35"><stop offset="0" stop-color="#3a1a12"/><stop offset=".7" stop-color="#120807"/><stop offset="1" stop-color="#040202"/></radialGradient><clipPath id="oc"><circle cx="'+cx+'" cy="'+cy+'" r="'+R+'"/></clipPath></defs>';
  s+='<circle cx="'+cx+'" cy="'+cy+'" r="'+(R+60)+'" fill="#a8352b" opacity=".12" filter="url(#blur30)"/>';
  s+='<g clip-path="url(#oc)"><circle cx="'+cx+'" cy="'+cy+'" r="'+R+'" fill="url(#ob)"/><g filter="url(#blur30)"><ellipse cx="'+(cx-50)+'" cy="'+(cy+30)+'" rx="170" ry="70" fill="#a8352b" opacity=".75" transform="rotate(-28 '+cx+' '+cy+')"/><ellipse cx="'+(cx+50)+'" cy="'+(cy-30)+'" rx="150" ry="55" fill="#e08a3a" opacity=".5" transform="rotate(24 '+cx+' '+cy+')"/><ellipse cx="'+cx+'" cy="'+(cy+120)+'" rx="120" ry="45" fill="#7a2a1c" opacity=".7"/></g>';
  for(var i=0;i<150;i++){var a=r()*6.283,d=Math.sqrt(r())*R*.95,k=r();s+='<circle cx="'+(cx+Math.cos(a)*d).toFixed(1)+'" cy="'+(cy+Math.sin(a)*d).toFixed(1)+'" r="'+(1+k*3).toFixed(1)+'" fill="'+(k>.6?GL:G)+'" opacity="'+(.35+k*.55).toFixed(2)+'"/>'}
  s+='<path d="M'+(cx-170)+' '+(cy-120)+'C'+(cx-130)+' '+(cy-200)+' '+(cx-40)+' '+(cy-235)+' '+(cx+30)+' '+(cy-232)+'C'+(cx-40)+' '+(cy-200)+' '+(cx-130)+' '+(cy-150)+' '+(cx-170)+' '+(cy-120)+'Z" fill="#fff" opacity=".2"/></g>';
  s+='<circle cx="'+cx+'" cy="'+cy+'" r="'+(R+14)+'" fill="none" stroke="'+GD+'" stroke-width="28"/><circle cx="'+cx+'" cy="'+cy+'" r="'+(R+26)+'" fill="none" stroke="'+G+'" stroke-width="3"/><circle cx="'+cx+'" cy="'+cy+'" r="'+(R+2)+'" fill="none" stroke="'+GL+'" stroke-width="2" opacity=".6"/>';
  [[0,-1],[1,0],[0,1],[-1,0]].forEach(function(p){s+='<circle cx="'+(cx+p[0]*(R+14))+'" cy="'+(cy+p[1]*(R+14))+'" r="17" fill="'+RD+'" stroke="'+GL+'" stroke-width="3"/>'});
  s+=ringText(cx,cy,R+112,44,.9,'rc');
  if(sc)s+="</g>";
  return s+finish();
}
