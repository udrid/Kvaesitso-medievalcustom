/* T4 weather dioramas: a little woodcut landscape (hills, oak, castle with banner) whose sky shows the weather.
   Drawn on a 128x52 grid, shown at 64x26 in the header. Stop-motion: every moving part is 2-6 hand-drawn
   frames swapped in hard steps (no smooth tweening), like paper cut-outs on a table. Vector only. */
(function(){
var G='#c9a55a',GD='#8c6d3a',GDD='#4a3a20',GL='#e3cf9c',RD='#a8352b',RDD='#6e1a14',INK='#0c0a07',MUTE='#a89a76';

/* colour palettes (owner picks one). gold = all gold line work; the others add pigment to the weather and land */
var BASE={hill1:'#17140e',hill2:'#0f0d09',edge:GD,edge2:GDD,grass:GD,leaf:'#1d1a14',leafEdge:GD,leafHatch:GDD,roof:RDD,
 sun:'#b8913c',rays:G,moon:GL,rain:MUTE,snow:GL,fog:MUTE,bolt:RD,boltEdge:GL,glow:'#e58a3c',sky:null};
function pal(o){var p={};for(var k in BASE)p[k]=BASE[k];for(k in o)p[k]=o[k];return p}
var PAL={
 gold:pal({}),
 accent:pal({sun:'#e0a83a',rays:'#e8b84a',rain:'#6f9fd8',snow:'#f4f1e8',bolt:'#f2c94c',boltEdge:'#fff1b0',glow:'#f0a040'}),
 pigment:pal({hill1:'#1c2112',hill2:'#12160b',edge:'#7d8a4a',edge2:'#4a5230',grass:'#8aa05a',leaf:'#34492a',leafEdge:'#8aa05a',leafHatch:'#5c7640',roof:'#8a2a1e',
  sun:'#dca23a',rays:'#e4b44a',rain:'#5f8fcf',snow:'#f4f1e8',bolt:'#f2c94c',boltEdge:'#fff1b0',glow:'#f0a040',fog:'#9aa4a0',
  sky:{clear:'#2c4466',clearnight:'#141b36',partly:'#2a3a52',cloudy:'#262a30',rain:'#1c2632',storm:'#1c1a2a',snow:'#2a3240',fog:'#2a2c2a'}}),
 vivid:pal({hill1:'#22301a',hill2:'#152010',edge:'#8fb050',edge2:'#55703a',grass:'#a0c060',leaf:'#3f6a2a',leafEdge:'#a0c060',leafHatch:'#6a9a48',roof:'#a8352b',
  sun:'#f0b030',rays:'#f7c84a',moon:'#f4ecd0',rain:'#4f8fe8',snow:'#ffffff',bolt:'#ffd23a',boltEdge:'#fff6c8',glow:'#ffb050',fog:'#a8b4b4',
  sky:{clear:'#3a6aa0',clearnight:'#1c2860',partly:'#36587e',cloudy:'#30363e',rain:'#22324a',storm:'#241e3a',snow:'#3a4a60',fog:'#3a3c3a'}})
};
var C=PAL.gold;
function sky(k,id){if(!C.sky)return '';return '<defs><radialGradient id="sk'+id+'" cx=".5" cy=".5" r=".5"><stop offset="0" stop-color="'+C.sky[k]+'" stop-opacity=".85"/><stop offset="1" stop-color="'+C.sky[k]+'" stop-opacity="0"/></radialGradient></defs><rect width="128" height="46" fill="url(#sk'+id+')"/>'}

/* frame swapper: frame k is visible during the k-th 1/n of the cycle */
var css='';[2,3,4,6].forEach(function(n){css+='@keyframes wxf'+n+'{0%{opacity:1}'+(100/n).toFixed(3)+'%,100%{opacity:0}}'});
function frames(list,dur){var n=list.length,s='';list.forEach(function(f,k){
 var d=((n-k)%n)*dur/n;s+='<g style="animation:wxf'+n+' '+dur+'s steps(1,end) infinite;animation-delay:-'+d.toFixed(3)+'s">'+f+'</g>'});return s}

/* ---------- landscape ---------- */
function hills(snow){var e=snow?GL:C.edge,e2=snow?'#bfb08a':C.edge2;
 return '<path d="M0 39C18 30 38 32 58 35S98 29 128 33V52H0Z" fill="'+C.hill1+'" stroke="'+e2+'" stroke-width=".8"/>'+
 '<path d="M34 35l3-1.4M40 35.4l3-1.2M46 35.8l3-1M64 34.6l3-1.2M70 33.8l3-1.2" stroke="'+GDD+'" stroke-width=".6" stroke-linecap="round"/>'+
 '<path d="M0 46C24 40 52 44 76 42S110 40 128 44V52H0Z" fill="'+C.hill2+'" stroke="'+e+'" stroke-width="1"/>'+
 '<path d="M103 38C98 42 86 43 74 45S52 49 46 52" stroke="'+GDD+'" stroke-width="1.6" stroke-linecap="round"/>'+
 '<path d="M56 46.5l.8-2 .8 2M62 45.6l.8-2 .8 2M90 43.4l.8-2 .8 2M8 47l.8-2 .8 2" stroke="'+C.grass+'" stroke-width=".6" stroke-linejoin="round"/>';
}
function tree(snow){
 return '<path d="M18 45V34M18 39l-3-3M18 37l3-2.5" stroke="'+GD+'" stroke-width="1.4" stroke-linecap="round"/>'+
 '<path d="M9 34C6 30 9 25 13 26C14 21 21 20 23 24C27 22 31 26 29 30C32 32 30 37 26 36C24 38 20 38 18 36C15 38 10 37 9 34Z" fill="'+C.leaf+'" stroke="'+C.leafEdge+'" stroke-width=".9" stroke-linejoin="round"/>'+
 '<path d="M12 31q2-2 4 0M19 27q2-2 4 0M21 32q2-2 4 0M14 35q1.5-1.5 3 0" stroke="'+C.leafHatch+'" stroke-width=".6" stroke-linecap="round"/>'+
 (snow?'<path d="M9.5 31C9 27 11.5 25.5 13.5 26.4C14.5 22 20.5 21 22.6 24.6C25.6 22.6 29.6 25.6 28.6 29C25 26.4 22 27.4 18.5 26.4C15 27.2 12 28.6 9.5 31Z" fill="'+GL+'" opacity=".9"/>':'');
}
function castle(win,snow){
 var w=function(x,y){return '<rect x="'+x+'" y="'+y+'" width="1.4" height="3" rx=".7" fill="'+win+'"/>'};
 return ''+
 /* side towers with conical oxblood roofs */
 '<path d="M88 37V24H95V37Z M110 37V22H117V37Z" fill="#1d1a14" stroke="'+GD+'" stroke-width=".9" stroke-linejoin="round"/>'+
 '<path d="M87 24L91.5 14.5L96 24Z M109 22L113.5 12.5L118 22Z" fill="'+C.roof+'" stroke="'+G+'" stroke-width=".8" stroke-linejoin="round"/>'+
 '<path d="M89.2 21.5L91.5 17M111.2 19.5L113.5 15" stroke="'+RD+'" stroke-width=".6"/>'+
 /* keep with crenellations */
 '<path d="M95 37V19H96.6V17H98.6V19H100.4V17H102.4V19H104.2V17H106.2V19H108V17H110V37Z" fill="#221d15" stroke="'+G+'" stroke-width=".9" stroke-linejoin="round"/>'+
 '<path d="M96 25H109M96 31H109" stroke="'+GDD+'" stroke-width=".5"/>'+
 '<path d="M100 37V32A2.5 2.5 0 0 1 105 32V37Z" fill="'+INK+'" stroke="'+GD+'" stroke-width=".7"/>'+
 '<path d="M100.8 33.5H104.2M100.8 35.2H104.2" stroke="'+GDD+'" stroke-width=".4"/>'+
 w(101.8,21.5)+w(90.8,27)+w(112.8,25.5)+
 (snow?'<path d="M89.2 20.8L91.5 15.6L93.8 20.8C92.6 19.8 90.4 19.8 89.2 20.8Z M111.2 18.8L113.5 13.6L115.8 18.8C114.6 17.8 112.4 17.8 111.2 18.8Z" fill="'+GL+'"/><path d="M95.3 18.8H110" stroke="'+GL+'" stroke-width="1"/>':'');
}
function banner(dur){
 var pole='<path d="M102.5 17V5.5" stroke="'+GL+'" stroke-width=".8"/><circle cx="102.5" cy="5.2" r=".8" fill="'+G+'"/>';
 var f=['M102.5 6.5C106 5 109.5 8 113.5 6.5L112 9.3L113.5 12C109.5 13.2 106 10.5 102.5 12Z',
        'M102.5 6.5C106 8 109.5 5.2 113.5 7.2L111.8 9.6L113.5 12.2C109.5 10.6 106 13 102.5 12Z',
        'M102.5 6.5C105.5 6 110.5 6.2 113 7.6L111.6 10L112.8 12.6C109.5 11.6 105.5 12.2 102.5 12Z'];
 return pole+frames(f.map(function(d){return '<path d="'+d+'" fill="'+RD+'" stroke="'+G+'" stroke-width=".6" stroke-linejoin="round"/><path d="M105.5 8.6l1.4 1.4M106.9 8.6l-1.4 1.4" stroke="'+GL+'" stroke-width=".5"/>'}),dur||0.9);
}
function land(o){o=o||{};return hills(o.snow)+tree(o.snow)+castle(o.win||INK,o.snow)+banner(o.flag)}

/* ---------- sky pieces ---------- */
function sun(x,y,faint){
 if(faint)return '<circle cx="'+x+'" cy="'+y+'" r="7.5" fill="'+GL+'" opacity=".22"/><circle cx="'+x+'" cy="'+y+'" r="7.5" stroke="'+GL+'" stroke-width=".6" opacity=".35"/>';
 function rays(rot,sc){var s='';for(var i=0;i<16;i++){s+='<g transform="rotate('+(i*22.5+rot)+') scale('+sc+')">'+(i%2?
   '<path d="M0 -8.6Q1.3 -10 0 -11.2Q-1.3 -12.4 0 -13.4" stroke="'+C.rays+'" stroke-width=".8" stroke-linecap="round"/>':
   '<path d="M-1.4 -8.6L0 -14.2L1.4 -8.6Z" fill="'+C.rays+'"/>')+'</g>'}return s}
 return '<g transform="translate('+x+' '+y+')">'+frames([rays(0,1),rays(4,1.05),rays(8,.97)],1.2)+
 '<circle r="7.6" fill="'+C.sun+'" stroke="'+GL+'" stroke-width="1"/><circle r="6.1" stroke="#8a6a22" stroke-width=".5"/>'+
 '<path d="M-3.6 -1.4q1.1-1.1 2.2 0M1.4 -1.4q1.1-1.1 2.2 0M0 -.8V1.6H-.9M-2.4 3.1Q0 4.9 2.4 3.1" stroke="#4a2e0c" stroke-width=".7" stroke-linecap="round" stroke-linejoin="round"/>'+
 '<circle cx="-3.6" cy="2" r=".9" fill="'+RD+'" opacity=".55"/><circle cx="3.6" cy="2" r=".9" fill="'+RD+'" opacity=".55"/></g>';
}
function moon(x,y){
 return '<g transform="translate('+x+' '+y+')"><path d="M3 -8.5A8.5 8.5 0 1 0 3 8.5A10 10 0 0 1 3 -8.5Z" fill="'+C.moon+'" stroke="'+G+'" stroke-width=".8"/>'+
 '<path d="M-5.2 -2.2q1.3 1 2.6 0M-3.4 2.6q-1 1.2-2.4 1" stroke="'+GD+'" stroke-width=".7" stroke-linecap="round"/>'+
 '</g>';
}
function star(x,y,r){return '<path transform="translate('+x+' '+y+')" d="M0 -'+r+'Q0 0 '+r+' 0Q0 0 0 '+r+'Q0 0 -'+r+' 0Q0 0 0 -'+r+'Z" fill="'+GL+'"/>'}
function cloud(x,y,s,fill,shade){
 return '<g transform="translate('+x+' '+y+') scale('+s+')">'+
 '<path d="M3 16C-1 16 -1 10.4 3.4 10.2C2.4 5 8.4 3 11.4 6.2C13.4 0 22.4 -.4 24.4 5C27.6 2 33.6 3.4 33.4 8.4C38.6 7.6 40.6 13.4 37 16Z" fill="'+fill+'" stroke="'+G+'" stroke-width="'+(1/s).toFixed(2)+'" stroke-linejoin="round"/>'+
 '<path d="M9 13.4C9 10.4 13 10 13.6 12.4M20.6 9.6C20.4 6.4 25.6 6 26 9M29.6 13.4C30 11 33.6 11.2 33.4 13.4" stroke="'+GD+'" stroke-width="'+(.7/s).toFixed(2)+'" stroke-linecap="round"/>'+
 (shade?'<path d="M5 14.6H13M16 14.6H28M30 14.6H35M8 12.6H10M24 12.6H27" stroke="'+GDD+'" stroke-width="'+(.6/s).toFixed(2)+'" stroke-linecap="round"/>':'')+'</g>';
}
function drifting(fn,dx,dur){return frames([0,dx/2,dx,dx/2].map(function(o){return '<g transform="translate('+o+' 0)">'+fn()+'</g>'}),dur)}
function birds(){var f=[];for(var k=0;k<4;k++){var up=k%2==0,x=k*1.6;
 var b=function(bx,by){return up?'<path d="M'+bx+' '+by+'q1.6-1.8 3.2 0q1.6-1.8 3.2 0" stroke="'+GL+'" stroke-width=".7" stroke-linecap="round"/>':
  '<path d="M'+bx+' '+(by-1)+'q1.6 1.6 3.2 1q1.6.6 3.2-1" stroke="'+GL+'" stroke-width=".7" stroke-linecap="round"/>'};
 f.push(b(58+x,11)+b(68+x,7.5))}return frames(f,1.6)}
function rain(n,slant,top,bot){var f=[];for(var k=0;k<3;k++){var s='';for(var j=0;j<n;j++){
 var x=28+j*(44/n)+k*1.4,y=top+((j*7+k*5)%(bot-top-5));
 s+='<path d="M'+x.toFixed(1)+' '+y+'l'+slant+' 4.5" stroke="'+C.rain+'" stroke-width="1" stroke-linecap="round"/>'}f.push(s)}return frames(f,.6)}
function ripples(){return frames(['<ellipse cx="44" cy="48" rx="2" ry=".6" stroke="'+C.rain+'" stroke-width=".5"/>',
 '<ellipse cx="60" cy="46.6" rx="2" ry=".6" stroke="'+C.rain+'" stroke-width=".5"/><ellipse cx="44" cy="48" rx="3.2" ry=".9" stroke="'+C.rain+'" stroke-width=".4" opacity=".6"/>',
 '<ellipse cx="34" cy="48.6" rx="2" ry=".6" stroke="'+C.rain+'" stroke-width=".5"/><ellipse cx="60" cy="46.6" rx="3.2" ry=".9" stroke="'+C.rain+'" stroke-width=".4" opacity=".6"/>'],.9)}
function snowfall(){var f=[];for(var k=0;k<4;k++){var s='';for(var j=0;j<11;j++){
 var x=8+j*10.5+((j*3+k)%3)-1,y=16+((j*9+k*4)%26);
 s+='<path transform="translate('+x+' '+y+')" d="M0 -1.4V1.4M-1.2 -.7L1.2 .7M-1.2 .7L1.2 -.7" stroke="'+C.snow+'" stroke-width=".55" stroke-linecap="round"/>'}f.push(s)}return frames(f,1.6)}
function fog(){var f=[];for(var k=0;k<3;k++){var o=k*3;f.push(
 '<path d="M'+(-12+o)+' 30C8 27 28 33 48 30S88 27 140 30" stroke="'+C.fog+'" stroke-width="3.2" stroke-linecap="round" opacity=".32"/>'+
 '<path d="M'+(-6-o)+' 38C14 35 36 41 58 38S100 35 140 38" stroke="'+C.fog+'" stroke-width="3.6" stroke-linecap="round" opacity=".38"/>'+
 '<path d="M'+(-10+o)+' 45C10 42 34 48 56 45S98 42 140 45" stroke="'+C.fog+'" stroke-width="3" stroke-linecap="round" opacity=".3"/>'+
 '<path d="M'+(4+o)+' 34H40M'+(60-o)+' 41H96M'+(20+o)+' 48H70" stroke="'+GL+'" stroke-width=".6" stroke-linecap="round" opacity=".45"/>')}
 return '<rect y="24" width="128" height="28" fill="#2a2418" opacity=".35"/>'+frames(f,1.8)}
function bolt(){return '<path d="M52 20L46 30.5H51L44.5 42L57 27.5H52L56.5 20Z" fill="'+C.bolt+'" stroke="'+C.boltEdge+'" stroke-width=".8" stroke-linejoin="round"/>'}

var SCENE={
 clear:function(){return sun(36,15)+birds()+land()},
 clearnight:function(){
  var tw=frames([star(12,8,1.8)+star(52,6,1.2)+star(70,14,1.6)+star(24,22,1),star(12,8,1)+star(52,6,2)+star(70,14,1)+star(24,22,1.6)],1.4);
  return moon(36,15)+tw+star(80,5,.9)+star(62,20,.8)+land({win:C.glow})+
   frames(['<rect x="101.8" y="21.5" width="1.4" height="3" rx=".7" fill="#ffc070"/>',''],1.2);},
 partly:function(){return sun(30,14)+drifting(function(){return cloud(34,10,.9,'#1d1a14',true)},4,2.4)+land()},
 cloudy:function(){return drifting(function(){return cloud(6,3,1,'#221d15',true)},4,2.8)+
  frames([-0,-2,-4,-2].map(function(o){return '<g transform="translate('+o+' 0)">'+cloud(46,11,.85,'#1d1a14',true)+'</g>'}),2.8)+land()},
 rain:function(){return cloud(20,2,1.25,'#14110c',true)+rain(9,-1.6,22,44)+land()+ripples()},
 storm:function(){return frames(['','','<rect width="128" height="40" fill="'+GL+'" opacity=".09"/>','','',''],2.4)+
  cloud(18,0,1.35,'#0d0b08',true)+cloud(54,5,.8,'#14110c',true)+rain(12,-2.4,22,44)+
  frames(['','',bolt(),'',bolt(),''],2.4)+land({flag:.45})},
 snow:function(){return cloud(22,2,1.2,'#1d1a14',true)+land({snow:true})+snowfall()},
 fog:function(){return sun(40,14,true)+land()+fog()}
};
window.WX={css:css,names:Object.keys(SCENE),palettes:Object.keys(PAL),svg:function(k,w,h,p){C=PAL[p||'gold'];
 return '<svg class="wx" width="'+(w||64)+'" height="'+(h||26)+'" viewBox="0 0 128 52" fill="none" aria-hidden="true">'+sky(k,k+(p||''))+SCENE[k]()+'</svg>'}};
var st=document.createElement('style');st.textContent=css;document.head.appendChild(st);
})();
