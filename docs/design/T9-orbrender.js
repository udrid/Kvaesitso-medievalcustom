/* T9 orb renderer: traces light through a glass ball per pixel (preview of the orb picked by the owner, 2a-3). Used by T9-orb11.html and T9-vibe2.html. */
function rnd(seed){var x=seed;return function(){x=(x*9301+49297)%233280;return x/233280}}
function dots(c,n,seed,r0,r1,big){var R=rnd(seed),s='';for(var i=0;i<n;i++){var a=R()*6.283,r=r0+R()*(r1-r0);s+='<circle cx="'+(r*Math.cos(a)).toFixed(1)+'" cy="'+(r*Math.sin(a)).toFixed(1)+'" r="'+(R()<.2?big:big*.5).toFixed(2)+'" fill="'+c+'" opacity="'+(.4+R()*.6).toFixed(2)+'"/>';}return s}
/* volumetric fog: fractal noise pushed through a colour ramp. Rendered once to a picture; moving it later = one layer. */
function fog(id,seed,fx,ramp,op,tx,ty){var t=function(a){return a.join(' ')};
 return '<filter id="'+id+'" x="-10%" y="-10%" width="120%" height="120%" color-interpolation-filters="sRGB"><feTurbulence type="fractalNoise" baseFrequency="'+fx+'" numOctaves="4" seed="'+seed+'"/><feColorMatrix type="matrix" values="1 0 0 0 0  1 0 0 0 0  1 0 0 0 0  0 0 0 0 1"/><feComponentTransfer><feFuncR type="table" tableValues="'+t(ramp[0])+'"/><feFuncG type="table" tableValues="'+t(ramp[1])+'"/><feFuncB type="table" tableValues="'+t(ramp[2])+'"/></feComponentTransfer></filter><rect x="-70" y="-70" width="140" height="140" filter="url(#'+id+')" opacity="'+op+'"/>'}
var BL='<filter id="b1" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation=".9"/></filter><filter id="b2" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="2.2"/></filter><filter id="b5" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="5"/></filter><filter id="b9" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="9"/></filter>';
/* the glass: thick bright rim (fresnel), dark band just inside it, window reflection, hot caustic where light leaves the far side, horizon reflection */
function glass(rim,hot){return '<defs><radialGradient id="gF" cx=".5" cy=".5"><stop offset=".72" stop-color="#000" stop-opacity="0"/><stop offset=".86" stop-color="#000" stop-opacity=".45"/><stop offset=".95" stop-color="'+rim+'" stop-opacity=".18"/><stop offset="1" stop-color="'+rim+'" stop-opacity=".62"/></radialGradient><linearGradient id="gW" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#fff" stop-opacity=".75"/><stop offset="1" stop-color="#fff" stop-opacity=".05"/></linearGradient><radialGradient id="gC" cx=".5" cy=".5"><stop offset="0" stop-color="'+hot+'" stop-opacity=".9"/><stop offset="1" stop-color="'+hot+'" stop-opacity="0"/></radialGradient><linearGradient id="gH" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#fff" stop-opacity="0"/><stop offset="1" stop-color="#fff" stop-opacity=".16"/></linearGradient></defs>'+
 '<circle r="60" fill="url(#gF)"/><circle r="58.2" fill="none" stroke="'+rim+'" stroke-opacity=".55" stroke-width="1.2" filter="url(#b1)"/><circle r="55" fill="none" stroke="#000" stroke-opacity=".25" stroke-width="2"/>'+
 '<path d="M-31 -30C-24 -42 -10 -50 4 -50L6 -45C-6 -44 -18 -38 -26 -28Z" fill="url(#gW)"/><path d="M-39 -20C-36 -26 -33 -29 -30 -32L-28 -29C-31 -26 -34 -22 -36 -17Z" fill="#fff" opacity=".45"/><circle cx="-18" cy="-36" r="1.6" fill="#fff" opacity=".9"/>'+
 '<path d="M28 44C38 38 46 28 50 16C48 30 40 42 28 50Z" fill="#fff" opacity=".55" filter="url(#b1)"/><ellipse cx="30" cy="42" rx="14" ry="9" fill="url(#gC)" transform="rotate(-35 30 42)"/>'+
 '<path d="M-52 18C-30 26 30 26 52 18C48 34 36 48 0 52C-36 48 -48 34 -52 18Z" fill="url(#gH)"/>'}
var RA={arc:[[0,0,.08,.5,1],[0,.06,.35,.85,1],[.03,.2,.65,1,1]],vio:[[.02,.1,.4,.9,1],[0,0,.1,.5,1],[.06,.25,.65,1,1]],amb:[[.03,.2,.75,1,1],[0,.03,.25,.8,1],[0,0,.04,.35,1]],bk:[[0,0,0,.05,.9],[0,0,0,.02,.35],[0,0,0,0,.05]]};
function arcs(c,n,seed,r0,r1){var R=rnd(seed),s='';for(var i=0;i<n;i++){var a=R()*6.283,x=r0*Math.cos(a),y=r0*Math.sin(a),p='M'+x.toFixed(1)+' '+y.toFixed(1),ang=a;for(var k=1;k<=4;k++){ang+=(R()-.5)*.9;var r=r0+(r1-r0)*k/4;p+='L'+(r*Math.cos(ang)).toFixed(1)+' '+(r*Math.sin(ang)).toFixed(1);}s+='<path d="'+p+'"/>';}return '<g fill="none" stroke="'+c+'" stroke-linejoin="round">'+s+'</g>'}
/* ---- per-pixel glass orb renderer (preview of what a GPU shader would do) ----
   sphere radius 1, viewer looking down -z. Per pixel: refract into the glass (ior 1.5), march through a
   swirling smoke volume lit by a heart at the centre, add the heart's glow, darken the thick edge,
   then mix in a reflected room (leaded arched window + candle) by Fresnel, plus a caustic where the
   window light leaves the far side. */
function H3(x,y,z){var h=Math.imul(x,374761393)^Math.imul(y,668265263)^Math.imul(z,1274126177);h=Math.imul(h^(h>>>13),1274126177);h^=h>>>16;return (h>>>0)/4294967296}
function vn(x,y,z){var xi=Math.floor(x),yi=Math.floor(y),zi=Math.floor(z),xf=x-xi,yf=y-yi,zf=z-zi,u=xf*xf*(3-2*xf),v=yf*yf*(3-2*yf),w=zf*zf*(3-2*zf);
 var a=H3(xi,yi,zi),b=H3(xi+1,yi,zi),c=H3(xi,yi+1,zi),d=H3(xi+1,yi+1,zi),e=H3(xi,yi,zi+1),f=H3(xi+1,yi,zi+1),g=H3(xi,yi+1,zi+1),h=H3(xi+1,yi+1,zi+1);
 var x1=a+(b-a)*u,x2=c+(d-c)*u,x3=e+(f-e)*u,x4=g+(h-g)*u,y1=x1+(x2-x1)*v,y2=x3+(x4-x3)*v;return y1+(y2-y1)*w}
function fbm(x,y,z,o){var s=0,a=.5,f=1,n=0;for(var i=0;i<o;i++){s+=a*vn(x*f,y*f,z*f);n+=a;f*=2.03;a*=.5}return s/n}
function sst(a,b,x){var t=Math.max(0,Math.min(1,(x-a)/(b-a)));return t*t*(3-2*t)}
function pal(p,t){t=Math.max(0,Math.min(1,t))*(p.length-1);var i=Math.min(p.length-2,Math.floor(t)),f=t-i;return [p[i][0]+(p[i+1][0]-p[i][0])*f,p[i][1]+(p[i+1][1]-p[i][1])*f,p[i][2]+(p[i+1][2]-p[i][2])*f]}
var AMBER=[[.10,.015,.008],[.45,.08,.02],[.95,.38,.07],[1,.72,.32],[1,.95,.82]];
var OXB=[[.08,.005,.01],[.32,.02,.03],[.62,.07,.05],[.95,.32,.12],[1,.8,.55]];
/* the room the glass reflects: dark, a leaded arched window up-left, a candle to the right */
function env(Rx,Ry,Rz,o){var ax=Math.atan2(Rx,Rz),ay=Math.asin(Math.max(-1,Math.min(1,Ry)));
 var r=.03+.05*Math.max(0,Ry)+.12*sst(-.05,-.5,Ry),g=.018+.03*Math.max(0,Ry)+.07*sst(-.05,-.5,Ry),b=.012+.015*Math.max(0,Ry)+.03*sst(-.05,-.5,Ry);
 var W=o.win||{x:-.6,y:.48,w:.42,h:.66},wx=(ax-W.x)/W.w,wy=(ay-W.y)/W.h,inside=Math.abs(wx)<.5&&wy>-.5&&wy<.5;
 /* wm: 'lead' = leaded window (default), 'soft' = soft glow where the window was, no frame; 'point' = only a small sharp glint */
 if(o.wm=='soft'){var sd=wx*wx/.16+wy*wy/.2;if(sd<12){var ks=Math.exp(-sd*1.3);r+=ks*6;g+=ks*5;b+=ks*3.6}inside=false}
 if(o.wm=='point'){var pd=(wx*wx+wy*wy)/.009;if(pd<12){var kp=Math.exp(-pd);r+=kp*16;g+=kp*14;b+=kp*11}inside=false}
 if(inside&&wy>.18){var ex=wx/.5,ey=(wy-.18)/.32;inside=ex*ex+ey*ey<1}
 if(inside){var lead=0,gx=(wx*W.w+wy*W.h)*13,gy=(wx*W.w-wy*W.h)*13,fx=gx-Math.floor(gx),fy=gy-Math.floor(gy);
  if(fx<.07||fy<.07)lead=1;if(Math.abs(wx)<.035||Math.abs(wy+.05)<.03)lead=1;if(Math.abs(wx)>.45||wy<-.45)lead=1;var e=lead?.08:1;var warm=.85+.15*Math.sin(wx*9+wy*7);
  r+=e*4.2*warm;g+=e*3.5*warm;b+=e*2.4*warm}
 var cd=Math.pow((ax-.82)/.07,2)+Math.pow((ay-.02)/.1,2);if(cd<9&&!o.nocandle){var k=Math.exp(-cd);r+=2.6*k;g+=1.4*k;b+=.45*k}
 return [r,g,b]}
function renderOrb(o){var S=o.S||260,cv=document.createElement('canvas');cv.width=cv.height=S;var cx=cv.getContext('2d'),img=cx.createImageData(S,S),D=img.data,eta=1/1.5,N=o.steps||24;
 for(var py=0;py<S;py++)for(var px=0;px<S;px++){var u=(px+.5)/S*2-1,Y=1-(py+.5)/S*2,r2=u*u+Y*Y,id=(py*S+px)*4,rad=Math.sqrt(r2);
  if(rad>1.0){D[id+3]=0;continue}
  var zf=Math.sqrt(Math.max(0,1-r2)),k=1-eta*eta*(1-zf*zf),m=eta*zf-Math.sqrt(Math.max(k,0)),Tx=m*u,Ty=m*Y,Tz=-eta+m*zf,tl=Math.sqrt(Tx*Tx+Ty*Ty+Tz*Tz);Tx/=tl;Ty/=tl;Tz/=tl;
  var tex=-2*(u*Tx+Y*Ty+zf*Tz),dt=tex/N,tr=1,cr=0,cg=0,cb=0,dmin=9;
  for(var i=0;i<N;i++){var t=(i+.5)*dt,x=u+Tx*t,y=Y+Ty*t,z=zf+Tz*t,rr=Math.sqrt(x*x+y*y+z*z);if(rr<dmin)dmin=rr;
   var ang=o.swirl*(1-rr)+y*o.twist,ca=Math.cos(ang),sa=Math.sin(ang),qx=(x*ca-z*sa)*o.freq,qz=(x*sa+z*ca)*o.freq,qy=y*o.freq*o.yst;
   var wx=fbm(qx+o.seed,qy,qz,3),wy=fbm(qx,qy+o.seed*1.7,qz+5.2,3),n=fbm(qx+o.warp*wx*2,qy+o.warp*wy*2,qz+o.seed,4);
   if(o.ridge){n=1-Math.abs(2*n-1);n=Math.pow(n,o.ridge)}
   var band=Math.exp(-Math.pow((n-o.th)/o.bw,2)),den=(band*o.veil+Math.max(0,n-o.th)*o.fill)*(1-rr*rr*.55)*o.dens*sst(o.m0,o.m1,fbm(qx*.45+o.seed*.3,qy*.45,qz*.45,3));
   var L=o.heart/(1+rr*rr*o.fall),e=den*L,c=pal(o.pal,Math.min(.74,e*o.cs));
   cr+=tr*c[0]*e*dt;cg+=tr*c[1]*e*dt;cb+=tr*c[2]*e*dt;tr*=Math.exp(-den*o.ab*dt)}
  var core=Math.exp(-Math.pow(dmin/o.rc,2))*o.ci,halo=Math.exp(-dmin/o.rh)*o.hi,vis=.35+.65*tr,hc=pal(o.pal,.78);
  var kc=pal(o.pal,.88);cr+=(core*kc[0]+halo*hc[0])*vis;cg+=(core*kc[1]+halo*hc[1])*vis;cb+=(core*kc[2]+halo*hc[2])*vis;
  cr+=o.bg[0]*tr;cg+=o.bg[1]*tr;cb+=o.bg[2]*tr;
  /* gold dust; dspir > 0 gathers it into a spiral band (angle + radius) */
  var dw=1;if(o.dspir){var th=Math.atan2(Y,u),bandv=Math.sin(th*2+rad*o.dspir*6.283);dw=.03+2.6*Math.pow(Math.max(0,bandv),5)}
  if(o.dust){var sp=H3(px>>1,py>>1,o.seed|0);if(sp>1-o.dust*dw*(1-r2*.6)){var s=.6+.8*H3(px,py,3);cr+=s*1.0;cg+=s*.78;cb+=s*.4}}
  var edge=.18+.82*sst(0,.6,zf);cr*=edge;cg*=edge;cb*=edge;
  var Rx=2*zf*u,Ry=2*zf*Y,Rz=2*zf*zf-1,ev=env(Rx,Ry,Rz,o),F=.07+.93*Math.pow(1-zf,5),Fs=F*o.refl+o.gloss;
  cr=cr*(1-F)+ev[0]*Fs;cg=cg*(1-F)+ev[1]*Fs;cb=cb*(1-F)+ev[2]*Fs;
  var cdot=rad>0?(u*.72-Y*.69)/rad:0,cau=Math.exp(-Math.pow((rad-.86)/.075,2))*sst(.55,1,cdot)*o.cau;cr+=cau;cg+=cau*.72;cb+=cau*.38;
  var rim=sst(.955,.995,rad)*(1-sst(.995,1,rad))*.35;cr+=rim*o.rimc[0];cg+=rim*o.rimc[1];cb+=rim*o.rimc[2];
  if(H3(px,py,11)<.0016){var dd=.35*(.4+F*3);cr+=dd;cg+=dd;cb+=dd}
  var ex=o.exp,Lm=Math.max(cr,cg,cb,1e-4),sc=(1-Math.exp(-Lm*ex))/Lm;cr*=sc;cg*=sc;cb*=sc;var wl=Math.max(0,Lm*ex-2.2)*.05;cr+=wl;cg+=wl;cb+=wl;var lu=.3*cr+.55*cg+.15*cb,sat=o.sat||1.45;cr=Math.max(0,lu+(cr-lu)*sat);cg=Math.max(0,lu+(cg-lu)*sat);cb=Math.max(0,lu+(cb-lu)*sat);
  var gr=(H3(px,py,5)-.5)*.035;
  D[id]=Math.max(0,Math.min(255,Math.pow(cr,1/1.25)*255+gr*255));D[id+1]=Math.max(0,Math.min(255,Math.pow(cg,1/1.25)*255+gr*255));D[id+2]=Math.max(0,Math.min(255,Math.pow(cb,1/1.25)*255+gr*255));
  D[id+3]=Math.round(255*Math.max(0,Math.min(1,(1-rad)*S/2)))}
 cx.putImageData(img,0,0);return cv.toDataURL('image/jpeg',.88)}

/* ---- the gold setting: shaded band, beaded inner edge, twisted-rope outer edge, engraved scrolls, gems ---- */
function P(r,a){return (r*Math.cos(a)).toFixed(2)+' '+(r*Math.sin(a)).toFixed(2)}
function arc(r,a0,a1){return 'M'+P(r,a0*Math.PI/180)+'A'+r+' '+r+' 0 0 1 '+P(r,a1*Math.PI/180)}
function gem(x,y,rr,id){return '<g transform="translate('+x+' '+y+')"><circle r="'+(rr+1.5)+'" fill="url(#bzG)" stroke="#2a1a08" stroke-width=".6"/>'+
 [0,90,180,270].map(function(d){return '<path transform="rotate('+(d+45)+')" d="M-.9 -'+(rr+1.3)+'L.9 -'+(rr+1.3)+'L0 -'+(rr-.6)+'Z" fill="#f3dfa0" stroke="#3a2810" stroke-width=".3"/>'}).join('')+
 '<circle r="'+rr+'" fill="url(#'+id+')"/><circle r="'+rr+'" fill="none" stroke="#1a0202" stroke-width=".5"/><ellipse cx="'+(-rr*.35)+'" cy="'+(-rr*.4)+'" rx="'+(rr*.38)+'" ry="'+(rr*.24)+'" fill="#fff" opacity=".85" transform="rotate(-30 '+(-rr*.35)+' '+(-rr*.4)+')"/><circle cx="'+(rr*.4)+'" cy="'+(rr*.35)+'" r="'+(rr*.14)+'" fill="#ffb0a0" opacity=".7"/></g>'}
function bez(kind){var s='<defs><linearGradient id="bzG" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#f4e2a8"/><stop offset=".3" stop-color="#c9a253"/><stop offset=".65" stop-color="#86652a"/><stop offset="1" stop-color="#33240c"/></linearGradient><radialGradient id="rbG" cx=".35" cy=".3"><stop offset="0" stop-color="#ffa090"/><stop offset=".35" stop-color="#c8281c"/><stop offset=".85" stop-color="#4a0806"/><stop offset="1" stop-color="#220302"/></radialGradient><radialGradient id="gaG" cx=".35" cy=".3"><stop offset="0" stop-color="#ff9a70"/><stop offset=".4" stop-color="#8a1a10"/><stop offset="1" stop-color="#1a0302"/></radialGradient><filter id="bzb" x="-20%" y="-20%" width="140%" height="140%"><feGaussianBlur stdDeviation="1.2"/></filter></defs>';
 s+='<circle r="51.6" fill="none" stroke="#000" stroke-opacity=".55" stroke-width="3" filter="url(#bzb)"/>';
 s+='<circle r="56.4" fill="none" stroke="url(#bzG)" stroke-width="7.6"/>';
 s+='<path d="'+arc(56.4,20,140)+'" stroke="#1a0e04" stroke-opacity=".45" stroke-width="7.6" fill="none"/>';
 s+='<path d="'+arc(58.3,195,285)+'" stroke="#fff8dc" stroke-opacity=".6" stroke-width="1.1" fill="none" filter="url(#b1)"/><path d="'+arc(54.6,200,270)+'" stroke="#fff8dc" stroke-opacity=".45" stroke-width=".7" fill="none"/>';
 var i;for(i=0;i<72;i++){var a=i*Math.PI/36;s+='<circle cx="'+(53.5*Math.cos(a)).toFixed(2)+'" cy="'+(53.5*Math.sin(a)).toFixed(2)+'" r=".85" fill="#e8c878" stroke="#3a2810" stroke-width=".3"/>';}
 for(i=0;i<150;i++){s+='<path transform="rotate('+(i*2.4)+') translate(0 -59.3) rotate(38)" d="M0 -1V1" stroke="'+(i%2?'#f6e2a0':'#6a4c1c')+'" stroke-width=".7" stroke-opacity=".85" stroke-linecap="round"/>';}
 if(kind!='claws'){for(i=0;i<20;i++){s+='<g transform="rotate('+(i*18+9)+') translate(0 -56.4)"><path d="M-4.6 .6C-4 -1.8 -1.6 -1.9 -1 0C-.4 1.9 2 1.8 2.6 -.4M2.6 -.4C3 -1.6 4.4 -1.4 4.4 -.2" stroke="#4a3414" stroke-opacity=".75" stroke-width=".42" fill="none" stroke-linecap="round"/><circle cx="-4.6" cy=".6" r=".35" fill="#4a3414"/></g>';}}
 if(kind=='studs'){[0,90,180,270].forEach(function(d){var a=(d-90)*Math.PI/180;s+=gem((56.4*Math.cos(a)).toFixed(2),(56.4*Math.sin(a)).toFixed(2),3.1,'rbG')});}
 if(kind=='garnets'){for(i=0;i<8;i++){var a2=(i*45-90)*Math.PI/180;s+=gem((56.4*Math.cos(a2)).toFixed(2),(56.4*Math.sin(a2)).toFixed(2),2,'gaG')}}
 if(kind=='claws'){for(i=0;i<8;i++){s+='<g transform="rotate('+(i*45+22.5)+')"><path d="M-3.6 -57C-3 -52 -1.4 -48.5 0 -46.5C1.4 -48.5 3 -52 3.6 -57Z" fill="url(#bzG)" stroke="#2a1a08" stroke-width=".7"/><path d="M-1 -56C-.6 -51 0 -48 0 -46" stroke="#fff4c8" stroke-opacity=".55" stroke-width=".6" fill="none"/></g>';}
  [0,90,180,270].forEach(function(d){var a=(d-90)*Math.PI/180;s+=gem((56.4*Math.cos(a)).toFixed(2),(56.4*Math.sin(a)).toFixed(2),2.8,'rbG')});}
 return s}
function sparks(c,n,seed,r0,r1){var R=rnd(seed),s='';for(var i=0;i<n;i++){var a=R()*6.283,r=r0+R()*(r1-r0),x=(r*Math.cos(a)).toFixed(1),y=(r*Math.sin(a)).toFixed(1),z=.5+R()*.8;s+='<circle cx="'+x+'" cy="'+y+'" r="'+(z*2.2).toFixed(1)+'" fill="'+c+'" opacity=".25" filter="url(#b1)"/><circle cx="'+x+'" cy="'+y+'" r="'+(z*.6).toFixed(2)+'" fill="#fff6dc"/>';}return s}
var BASE={m0:.4,m1:.62,bw:.065,veil:1,fill:.3,S:300,steps:24,pal:AMBER,swirl:2.2,twist:1.2,freq:2.9,yst:1.0,warp:.9,seed:3.1,th:.5,dens:8,ab:5.5,heart:3.2,fall:14,cs:.7,rc:.12,ci:1.6,rh:.2,hi:.8,bg:[.01,.004,.002],refl:1,gloss:0,cau:.55,rimc:[1,.85,.6],exp:1.9};
function O(p){var o={};for(var k in BASE)o[k]=BASE[k];for(var k2 in p)o[k2]=p[k2];return o}
/* soft star-glow at the heart: bloom, a faint four-point star, a small bright point */
function flare(c,s){s=s||1;var r=9*s,q=1.6*s;return '<g><circle r="'+(10*s)+'" fill="'+c+'" opacity=".4" filter="url(#b5)"/><path d="M0 -'+r+'Q'+q+' -'+q+' '+r+' 0Q'+q+' '+q+' 0 '+r+'Q-'+q+' '+q+' -'+r+' 0Q-'+q+' -'+q+' 0 -'+r+'Z" fill="'+c+'" opacity=".55" filter="url(#b1)"/><circle r="'+(2.6*s)+'" fill="#fff6e0" filter="url(#b1)"/><circle r="'+(1.3*s)+'" fill="#fffcf4"/></g>'}
/* each orb picture is rendered once and kept in one hidden <svg>; every place that shows it points at it */
var ORBIMG={},ORBDEFS=(function(){var s=document.createElementNS('http://www.w3.org/2000/svg','svg');s.setAttribute('width','0');s.setAttribute('height','0');s.style.position='absolute';s.innerHTML='<defs></defs>';document.body.appendChild(s);return s.firstChild})();
function orbRef(o){var key=JSON.stringify(o);if(!ORBIMG[key]){var id='oimg'+Object.keys(ORBIMG).length;ORBIMG[key]=id;
 var im=document.createElementNS('http://www.w3.org/2000/svg','image');im.setAttribute('id',id);im.setAttribute('href',renderOrb(o));im.setAttribute('x','-60');im.setAttribute('y','-60');im.setAttribute('width','120');im.setAttribute('height','120');ORBDEFS.appendChild(im)}
 return '<use href="#'+ORBIMG[key]+'"/>'}
/* each gold setting is drawn once in the hidden <svg> too, so the page stays small */
var BEZ={};function bezRef(kind){if(!BEZ[kind]){var g=document.createElementNS('http://www.w3.org/2000/svg','g');g.setAttribute('id','bez-'+kind);g.innerHTML='<defs>'+BL+'</defs>'+bez(kind);ORBDEFS.appendChild(g);BEZ[kind]=1}return '<use href="#bez-'+kind+'"/>'}
function orb(o,bk,sp,fl){return '<defs>'+BL+'</defs>'+orbRef(o)+(fl===0?'':flare(fl||'#ffd890',o.fs||1))+(sp||'')+bezRef(bk)}
/* gold dust laid along two spiral arms (drawn over the glass picture) */
function spiralDust(n,seed){var R=rnd(seed),s='';for(var arm=0;arm<2;arm++)for(var i=0;i<n;i++){var t=i/n,a=arm*Math.PI+t*4.6+(R()-.5)*.35,r=8+t*40+(R()-.5)*5,x=(r*Math.cos(a)).toFixed(1),y=(r*Math.sin(a)*.8).toFixed(1),z=R();
 s+=(z>.85?'<circle cx="'+x+'" cy="'+y+'" r="1.8" fill="#ffc870" opacity=".3" filter="url(#b1)"/>':'')+'<circle cx="'+x+'" cy="'+y+'" r="'+(.35+z*.55).toFixed(2)+'" fill="'+(z>.5?'#ffe2a0':'#e8b860')+'" opacity="'+(.55+z*.45).toFixed(2)+'"/>';}return '<g transform="rotate(-20)">'+s+'</g>'}
var OX2={pal:OXB,seed:21.4,dens:6,heart:1.8,fall:12,ci:1.8,rc:.1,hi:.8,rh:.18,dust:.006,cs:.6,cau:.45,rimc:[1,.7,.6]};
function X(p){var o={};for(var k in OX2)o[k]=OX2[k];for(var k2 in p)o[k2]=p[k2];return O(o)}
var LOOKS=[
 {id:'s',name:'2a-1. Soft window light',glow:'rgba(170,30,25,.48)',
  desc:'Your pick, with the window\'s cross frame removed: the highlight up-left is now a soft oval of light, so the glass still reads as glass but nothing draws the eye away from the smoke and gold dust.',
  cost:'<b>LOW</b>: a pre-drawn slow loop of the smoke (8 fps); dust twinkles on a tiny layer. Still version: none.',
  svg:function(){return orb(X({wm:'soft'}),'studs',sparks('#f3cf80',8,7,10,46),'#ff9a60')}},
 {id:'p',name:'2a-2. Single glint',glow:'rgba(170,30,25,.48)',
  desc:'No window at all: only one small, sharp glint of light up-left (and the faint candle on the right). The cleanest look; the ball reads darker and deeper.',
  cost:'<b>LOW</b>, same as 2a-1.',
  svg:function(){return orb(X({wm:'point'}),'studs',sparks('#f3cf80',8,7,10,46),'#ff9a60')}},
 {id:'d',name:'2a-3. Heavy gold dust',glow:'rgba(190,60,30,.5)',
  desc:'About three times as much gold dust, with a few bigger glittering grains, like the ball is full of powdered gold stirred into blood. The richest and busiest.',
  cost:'<b>LOW</b>: dust twinkle is the only extra work (tiny layer, 6 fps).',
  svg:function(){return orb(X({wm:'soft',dust:.017,seed:23.1}),'studs',sparks('#ffd890',18,11,8,50),'#ffa060')}},
 {id:'v',name:'2a-4. Gold dust spiral',glow:'rgba(180,45,28,.5)',
  desc:'The gold dust gathers into two slow spiral arms around the heart, like a galaxy of gold inside the red smoke. A turning spiral is also a natural, very calm animation.',
  cost:'<b>LOW</b>: the spiral turns slowly (8 fps, one layer). Still version: none.',
  svg:function(){return orb(X({wm:'soft',dust:.003,swirl:4,seed:17.2}),'studs',spiralDust(70,5)+sparks('#ffd890',4,5,12,44),'#ffa060')}},
 {id:'g',name:'2a-5. Molten gold heart',glow:'rgba(200,90,30,.5)',
  desc:'The heart is bigger and burns molten gold instead of red, so the centre glows like a coin in a forge with oxblood smoke around it. Warmer, and the gold ties to the setting.',
  cost:'<b>LOW</b>: the heart can pulse slowly (one small layer, 8 fps).',
  svg:function(){return orb(X({wm:'soft',heart:2.6,rc:.15,ci:2.4,hi:1.2,rh:.24,seed:31.7}),'studs',sparks('#ffe0a0',10,13,10,46),'#ffc870')}},
 {id:'k',name:'2a-6. Dark and deep',glow:'rgba(140,20,20,.42)',
  desc:'Darker glass with bigger, slower wisps of red smoke, sparse dust and a small coal heart, with only the single glint. The most ominous; the gold ring stands out more.',
  cost:'<b>VERY LOW</b>: nearly still; the heart breathes (one small layer, 8 fps).',
  svg:function(){return orb(X({wm:'point',dens:5,heart:1.3,dust:.003,freq:2.2,bw:.09,seed:8.8,ci:1.4,rc:.08}),'studs',sparks('#f3cf80',4,3,14,40),'#ff7a40')}},
 {id:'w',name:'2a-7. As before (with the window), for comparison',glow:'rgba(170,30,25,.48)',
  desc:'Your pick exactly as it was, with the leaded window reflection. The cross you noticed is the window\'s frame; it is only there to make the ball read as real glass.',
  cost:'<b>LOW</b>, same as 2a-1.',
  svg:function(){return orb(X({}),'studs',sparks('#f3cf80',8,7,10,46),'#ff9a60')}}
];
