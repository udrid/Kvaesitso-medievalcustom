/* T4 weather dioramas: 64x26 paper-cutout scenes, stop-motion (stepped) animation. Vector, drawn here. */
(function(){
var G='#c9a55a',GD='#8c6d3a',RD='#a8352b',GL='#e3cf9c',DK='#1d1a14';
var css='.wx .rays,.wx .flake,.wx .tw,.wx .tw2{transform-box:fill-box;transform-origin:center}'+
'.wx .rays{animation:wxr 1.2s steps(1,end) infinite}@keyframes wxr{0%{transform:rotate(0)}33%{transform:rotate(9deg)}66%{transform:rotate(-7deg)}}'+
'.wx .drift{animation:wxd 2.4s steps(1,end) infinite}@keyframes wxd{0%{transform:translateX(0)}25%{transform:translateX(2px)}50%{transform:translateX(4px)}75%{transform:translateX(2px)}}'+
'.wx .drift2{animation:wxd2 2.4s steps(1,end) infinite}@keyframes wxd2{0%{transform:translateX(0)}25%{transform:translateX(-2px)}50%{transform:translateX(-4px)}75%{transform:translateX(-2px)}}'+
'.wx .fall{animation:wxf .9s steps(1,end) infinite}@keyframes wxf{0%{transform:translateY(0)}33%{transform:translateY(3px)}66%{transform:translateY(6px)}}'+
'.wx .fall2{animation:wxf .9s steps(1,end) infinite;animation-delay:-.45s}'+
'.wx .flake{animation:wxs 1.5s steps(1,end) infinite}@keyframes wxs{0%{transform:translate(0,0)}25%{transform:translate(1px,2px)}50%{transform:translate(-1px,4px)}75%{transform:translate(1px,6px)}}'+
'.wx .flash{animation:wxb 2.4s steps(1,end) infinite}@keyframes wxb{0%{opacity:0}60%{opacity:1}72%{opacity:0}80%{opacity:1}88%{opacity:0}}'+
'.wx .tw{animation:wxt 1.6s steps(1,end) infinite}@keyframes wxt{0%{opacity:1}50%{opacity:.15}}'+
'.wx .tw2{animation:wxt 1.6s steps(1,end) infinite;animation-delay:-.8s}'+
'.wx .fog{animation:wxd 3s steps(1,end) infinite}.wx .fog2{animation:wxd2 3s steps(1,end) infinite}';
function cloud(x,y,s,fill,cls){return '<g transform="translate('+x+' '+y+')"><g class="'+(cls||'')+'"><path transform="scale('+s+')" d="M0 7C0 4 2 2.6 4 3C4.6 0.4 8.4 -0.6 10 2C12.6 1 15 3 14.6 5.6C16 6 16 8.6 14 8.6H2C.8 8.6 0 8 0 7Z" fill="'+fill+'" stroke="'+G+'" stroke-width=".8" stroke-linejoin="round"/></g></g>'}
function sun(x,y,cls){var r='';for(var i=0;i<8;i++){r+='<path d="M0 -8V-5.2" stroke="'+G+'" stroke-width="1.1" stroke-linecap="round" transform="rotate('+i*45+')"/>'}
 return '<g transform="translate('+x+' '+y+')"><g class="rays">'+r+'</g><circle r="3.8" fill="#a07a30" stroke="'+GL+'" stroke-width=".9"/></g>'}
function moon(x,y){return '<path transform="translate('+x+' '+y+')" d="M2 -6A6 6 0 1 0 2 6A4.6 4.6 0 1 1 2 -6Z" fill="'+GL+'" fill-opacity=".9" stroke="'+G+'" stroke-width=".6"/>'}
function star(x,y,c){return '<path class="'+c+'" transform="translate('+x+' '+y+')" d="M0 -2Q0 0 2 0Q0 0 0 2Q0 0 -2 0Q0 0 0 -2Z" fill="'+G+'"/>'}
function drops(n,cls){var s='';for(var i=0;i<n;i++){s+='<path class="'+(i%2?'fall':'fall2')+'" d="M'+(18+i*4)+' 13.5l-1 3" stroke="#a89a76" stroke-width="1" stroke-linecap="round" opacity=".85"/>'}return s}
function flakes(){var s='';[[18,14],[23,15],[28,13.5],[33,15]].forEach(function(p,i){s+='<g class="flake" style="animation-delay:-'+(i*.37)+'s"><circle cx="'+p[0]+'" cy="'+p[1]+'" r="1.1" fill="'+GL+'"/></g>'});return s}
/* ground + tiny castle silhouette: the setting for every scene */
var GROUND='<path d="M0 26V22C8 19 14 21 22 20S40 18 48 19L64 21V26Z" fill="#14110c" stroke="'+GD+'" stroke-width=".8"/>'+
 '<path d="M50 20V12H52V13.5H54V12H56V13.5H58V12H60V20Z M54 20V17H56V20Z" fill="#1d1a14" stroke="'+GD+'" stroke-width=".8" stroke-linejoin="round"/><path d="M55 12V8M55 8L59 9.5L55 11" stroke="'+RD+'" stroke-width=".9" fill="'+RD+'"/>';
var SCENE={
 clear:function(){return sun(22,10)},
 clearnight:function(){return moon(22,10)+star(34,6,'tw')+star(12,7,'tw2')+star(40,13,'tw')+star(8,15,'tw')},
 partly:function(){return sun(20,10)+cloud(24,7,1.05,DK,'drift')},
 cloudy:function(){return cloud(10,3,1.2,'#26211a','drift')+cloud(28,9,1.1,DK,'drift2')},
 rain:function(){return cloud(14,1,1.35,'#26211a')+drops(5)},
 storm:function(){return cloud(14,1,1.35,'#1a1612')+'<path class="flash" d="M25 11L21 16H24L22 22L28 15H25L27 11Z" fill="'+RD+'" stroke="'+G+'" stroke-width=".7" stroke-linejoin="round"/>'},
 snow:function(){return cloud(14,1,1.35,'#26211a')+flakes()},
 fog:function(){return '<path class="fog" d="M6 8H34M12 12H42M4 16H30" stroke="'+GD+'" stroke-width="1.6" stroke-linecap="round" opacity=".8"/><path class="fog2" d="M14 6H40M8 14H36" stroke="'+G+'" stroke-width="1" stroke-linecap="round" opacity=".55"/>'}
};
window.WX={css:css,names:Object.keys(SCENE),svg:function(k,w,h){
 return '<svg class="wx" width="'+(w||64)+'" height="'+(h||26)+'" viewBox="0 0 64 26" fill="none" aria-hidden="true">'+SCENE[k]()+GROUND+'</svg>'}};
var st=document.createElement('style');st.textContent=css;document.head.appendChild(st);
})();
