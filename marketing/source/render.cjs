// Renders the listing artwork in marketing/images/ from real in-game captures, the mod's own logo and the bundled
// OFL fonts. Usage (from the repository root):
//   NODE_PATH="$(npm root -g)" node marketing/source/render.cjs [name...]
// Requires the `playwright` package with a Chromium build available.
const path = require('path');
const fs = require('fs');
const { chromium } = require('playwright');

// The release and the supported Minecraft versions. Adding a version is one line here
// (plus one row in the compatibility table of curseforge/description.md).
const RELEASE = { version: '1.0', name: 'Sweet Home' };
const VERSIONS = [
  { minecraft: '1.20.1', loaders: ['Forge', 'Fabric'] },
  { minecraft: '1.21.1', loaders: ['NeoForge', 'Fabric'] },
  { minecraft: '26.2', loaders: ['NeoForge', 'Fabric'] },
  { minecraft: '26.3', loaders: ['NeoForge', 'Fabric'] },
];
const LOADERS = ['Fabric', 'Forge', 'NeoForge'].filter((loader) => VERSIONS.some((v) => v.loaders.includes(loader)));

const root = path.resolve(__dirname, '..', '..');
const out = path.join(root, 'marketing', 'images');
const shots = path.join(out, 'screenshots');
const fonts = path.join(__dirname, 'fonts');
const url = (file) => 'file://' + file;
const shot = (name) => url(path.join(shots, name));
const source = (name) => url(path.join(__dirname, name));

// Colourways from block/FurnitureColor.java (name, swatch, the dye that paints it).
const COLOURS = [['Baby Pink', '#f7b8cf', 'pink dye'], ['White', '#f8f4f1', 'white dye'], ['Cream', '#f5e5c6', 'yellow dye'],
  ['Lavender', '#cfbdf2', 'purple dye'], ['Mint', '#b6ead2', 'lime dye'], ['Baby Blue', '#b9daf7', 'light blue dye']];
// Sets from catalog/FurnitureSet.java (name, colour, pieces).
const SETS = [['Princess Bedroom', '#f7b8cf', 17], ['Strawberry Cottage', '#f2a3ba', 17], ['Lavender Dreams', '#cfbdf2', 16],
  ['Angelcore Room', '#b9daf7', 16], ['Pastel Kitchen', '#b6ead2', 13]];

const css = `
@font-face { font-family: BlDisplay; src: url('${url(path.join(fonts, 'fredoka.ttf'))}'); }
@font-face { font-family: BlText; src: url('${url(path.join(fonts, 'nunito.ttf'))}'); font-weight: 400; }
@font-face { font-family: BlText; src: url('${url(path.join(fonts, 'nunito-extrabold.ttf'))}'); font-weight: 800; }
:root { --text:#5b3d5f; --dim:#94789a; --plum:#4a2c50; --pink:#f59cbc; --pink-deep:#e2648f; --lav:#b7a2ee; --lav-deep:#8f74d8;
        --mint:#93dcc2; --sky:#9cc8f5; --gold:#f3c45e; --cream:#fff8f4; --line:rgba(226,100,143,.28); }
* { box-sizing: border-box; }
html, body { margin: 0; background: var(--cream); color: var(--text); font-family: BlText, sans-serif; }
.stage { position: relative; overflow: hidden; background: linear-gradient(160deg, #fff3f6 0%, #fbeefb 50%, #f1ecff 100%); }
.display { font-family: BlDisplay, sans-serif; }
.candy { background: linear-gradient(100deg, #ef6f9c 0%, #f08bb4 35%, #b48ef0 75%, #8f74d8 100%);
         -webkit-background-clip: text; background-clip: text; color: transparent; }
.soft { filter: drop-shadow(0 3px 0 rgba(255,255,255,.95)) drop-shadow(0 10px 24px rgba(226,100,143,.25)); }
.kicker { font-family: BlDisplay, sans-serif; color: var(--pink-deep); letter-spacing: .14em; font-size: 24px; text-transform: uppercase; }
.heart { display: inline-block; width: 22px; height: 20px; margin: 0 12px 0 0; vertical-align: -2px;
         background: var(--pink-deep); -webkit-mask: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 22 20'><path d='M11 19.5 1.8 10.6C-.6 8.2-.6 4.3 1.8 2c2.3-2.3 6-2.3 8.3 0L11 2.9l.9-.9c2.3-2.3 6-2.3 8.3 0 2.4 2.3 2.4 6.2 0 8.6Z'/></svg>") center / contain no-repeat;
         mask: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 22 20'><path d='M11 19.5 1.8 10.6C-.6 8.2-.6 4.3 1.8 2c2.3-2.3 6-2.3 8.3 0L11 2.9l.9-.9c2.3-2.3 6-2.3 8.3 0 2.4 2.3 2.4 6.2 0 8.6Z'/></svg>") center / contain no-repeat; }
.rule { height: 4px; border-radius: 2px; background: radial-gradient(circle, var(--pink) 0 2px, transparent 2.5px) 0 0 / 14px 4px repeat-x; }
.frame { position: relative; border: 7px solid #fff; border-radius: 22px; overflow: hidden; background: #fff;
         box-shadow: 0 0 0 2px rgba(226,100,143,.35), 0 26px 60px rgba(120,60,120,.28), 0 6px 14px rgba(120,60,120,.12); }
.frame > img { display: block; width: 100%; }
canvas.motes { position: absolute; left: 0; top: 0; width: 100%; height: 100%; pointer-events: none; }
.wash { position: absolute; inset: 0; background: radial-gradient(ellipse at 70% 45%, rgba(255,255,255,0) 30%, rgba(255,240,246,.55) 100%); }
.backdrop { position: absolute; inset: -40px; background-size: cover; background-position: center; filter: blur(18px) brightness(1.18) saturate(1.05); }
.pill { display: inline-block; border-radius: 999px; padding: 9px 22px 7px; margin-right: 12px; font-family: BlDisplay, sans-serif; font-size: 22px;
        color: #fff; background: linear-gradient(100deg, #ef7ba5, #b394ef); box-shadow: 0 6px 16px rgba(180,90,150,.3); }
.pill.ghost { color: var(--pink-deep); background: rgba(255,255,255,.85); box-shadow: 0 0 0 2px rgba(226,100,143,.5), 0 6px 16px rgba(180,90,150,.15); }
.label { position: absolute; font-family: BlDisplay, sans-serif; font-size: 23px; color: var(--plum); background: rgba(255,255,255,.95);
         border-radius: 999px; padding: 8px 20px 6px; white-space: nowrap; box-shadow: 0 0 0 2px rgba(226,100,143,.55), 0 10px 26px rgba(120,60,120,.3); z-index: 2; }
.label small { font-family: BlText, sans-serif; font-size: 18px; color: var(--dim); margin-left: 8px; }
ul.points { list-style: none; padding: 0; margin: 0; }
ul.points li { position: relative; padding-left: 38px; margin: 0 0 16px; font-size: 27px; line-height: 1.3; color: var(--text); }
ul.points li::before { content: ''; position: absolute; left: 0; top: 8px; width: 22px; height: 20px; background: var(--pink);
  -webkit-mask: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 22 20'><path d='M11 19.5 1.8 10.6C-.6 8.2-.6 4.3 1.8 2c2.3-2.3 6-2.3 8.3 0L11 2.9l.9-.9c2.3-2.3 6-2.3 8.3 0 2.4 2.3 2.4 6.2 0 8.6Z'/></svg>") center / contain no-repeat;
  mask: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 22 20'><path d='M11 19.5 1.8 10.6C-.6 8.2-.6 4.3 1.8 2c2.3-2.3 6-2.3 8.3 0L11 2.9l.9-.9c2.3-2.3 6-2.3 8.3 0 2.4 2.3 2.4 6.2 0 8.6Z'/></svg>") center / contain no-repeat; }
ul.points li:nth-child(3n+2)::before { background: var(--lav); }
ul.points li:nth-child(3n)::before { background: var(--mint); }
b.hi { color: var(--plum); font-weight: 800; }
.stats { display: flex; gap: 14px; margin-top: 22px; }
.stats > div { flex: 1; padding: 14px 20px 12px; border-radius: 18px; background: rgba(255,255,255,.92);
               box-shadow: 0 0 0 2px rgba(226,100,143,.25), 0 14px 34px rgba(120,60,120,.18); text-align: center; }
.stats .t { font-family: BlText, sans-serif; font-weight: 800; font-size: 16px; letter-spacing: .1em; text-transform: uppercase; color: var(--dim); }
.stats .v { font-family: BlDisplay, sans-serif; font-size: 34px; color: var(--plum); margin-top: 2px; white-space: nowrap; }
.items { display: flex; gap: 12px; margin-top: 22px; }
.items > div { flex: 1; border-radius: 18px; background: rgba(255,255,255,.92); padding: 12px 6px 10px; text-align: center;
               box-shadow: 0 0 0 2px rgba(226,100,143,.25), 0 14px 34px rgba(120,60,120,.18); }
.items canvas, .items img { display: block; margin: 0 auto; image-rendering: pixelated; }
.items .n { font-family: BlDisplay, sans-serif; font-size: 17px; line-height: 1.1; color: var(--plum); margin-top: 6px; }
pre.code { margin: 0; font-family: 'DejaVu Sans Mono', monospace; font-size: 22px; line-height: 1.5; color: var(--text); }
`;

// Drifting cherry petals and golden sparkles, the pastel counterpart of the sibling kits' motes.
// Deterministic so renders are reproducible.
const motes = (count = 46, seed = 7) => `<canvas class="motes"></canvas><script>
(() => { const c = document.currentScript.previousElementSibling; const r = c.getBoundingClientRect();
  c.width = r.width; c.height = r.height; const g = c.getContext('2d'); let s = ${seed};
  const rnd = () => (s = (s * 16807) % 2147483647) / 2147483647;
  const sparkle = (x, y, z, a) => { g.save(); g.translate(x, y); g.fillStyle = 'rgba(255,214,110,' + a + ')';
    g.beginPath(); g.moveTo(0, -z * 3); g.quadraticCurveTo(0, 0, z * 3, 0); g.quadraticCurveTo(0, 0, 0, z * 3);
    g.quadraticCurveTo(0, 0, -z * 3, 0); g.quadraticCurveTo(0, 0, 0, -z * 3); g.fill(); g.restore(); };
  const petal = (x, y, z, a, rot, pink) => { g.save(); g.translate(x, y); g.rotate(rot);
    const h = g.createLinearGradient(-z * 4, 0, z * 4, 0); h.addColorStop(0, 'rgba(' + pink + ',' + a + ')'); h.addColorStop(1, 'rgba(255,240,246,' + a + ')');
    g.fillStyle = h; g.beginPath(); g.ellipse(0, 0, z * 4.2, z * 2.4, 0, 0, Math.PI * 2); g.fill(); g.restore(); };
  for (let i = 0; i < ${count}; i++) { const x = rnd() * c.width, y = rnd() * c.height, a = .45 + rnd() * .45, z = 1.4 + rnd() * 2.2;
    if (rnd() < .32) sparkle(x, y, z, a); else petal(x, y, z, a, rnd() * Math.PI, rnd() < .5 ? '246,150,186' : '250,184,210'); } })();
</script>`;

const page = (width, height, body, transparent = false) => `<!doctype html><html><head><meta charset="utf-8"><style>${css}
body { width: ${width}px; height: ${height}px; } .stage { width: ${width}px; height: ${height}px; }
${transparent ? 'html, body, .stage { background: transparent; }' : ''}</style></head>
<body><div class="stage">${body}</div></body></html>`;

const heartSvg = (fill, size) => `<svg width="${size}" height="${Math.round(size * 20 / 22)}" viewBox="0 0 22 20" style="display:block"><path fill="${fill}" stroke="#fff" stroke-width="1.6" d="M11 19.5 1.8 10.6C-.6 8.2-.6 4.3 1.8 2c2.3-2.3 6-2.3 8.3 0L11 2.9l.9-.9c2.3-2.3 6-2.3 8.3 0 2.4 2.3 2.4 6.2 0 8.6Z"/></svg>`;

// A section heading: a candy ribbon with the title, readable on CurseForge's light and dark themes.
const header = (title) => page(1400, 120, `
  <div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:22px">
    <div class="rule" style="width:240px;opacity:.9;-webkit-mask-image:linear-gradient(90deg,transparent,#000);mask-image:linear-gradient(90deg,transparent,#000)"></div>
    ${heartSvg('#b7a2ee', 30)}
    <div style="position:relative;padding:14px 54px 12px;border-radius:999px;background:linear-gradient(100deg,#f592b7 0%,#f3a6c6 45%,#c3adf3 100%);
                box-shadow:0 0 0 4px #fff,0 0 0 6px rgba(226,100,143,.45),0 12px 28px rgba(120,60,120,.3)">
      <div class="display" style="font-size:46px;line-height:1;color:#fff;white-space:nowrap;text-shadow:0 3px 0 rgba(190,80,130,.45)">${title}</div></div>
    ${heartSvg('#f59cbc', 30)}
    <div class="rule" style="width:240px;opacity:.9;-webkit-mask-image:linear-gradient(90deg,#000,transparent);mask-image:linear-gradient(90deg,#000,transparent)"></div>
  </div>`, true);

// A feature card: copy on the left, artwork on the right. Bullets never end on a one-word line.
const card = ({ kicker, title, points, art, width = 1600, height = 900, artWidth = 980 }) => page(width, height, `
  <div class="backdrop" style="background-image:url('${source('backdrop.jpg')}');opacity:.5"></div>
  <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(255,247,250,.93) 0%,rgba(255,247,250,.86) 34%,rgba(250,240,255,.55) 62%,rgba(245,240,255,.35) 100%)"></div>
  <div class="wash"></div>${motes()}
  <div style="position:absolute;left:76px;top:0;bottom:0;width:${width - artWidth - 176}px;display:flex;flex-direction:column;justify-content:center">
    <div class="kicker"${kicker.length > 22 ? ' style="font-size:20px;letter-spacing:.1em"' : ''}><span class="heart"></span>${kicker}</div>
    <div class="display candy soft" style="font-size:66px;line-height:1.02;margin:16px 0 22px">${title}</div>
    <div class="rule" style="width:70%;margin-bottom:30px"></div>
    <ul class="points">${points.map((point) => `<li>${point.replace(/ (\S+)$/, '&nbsp;$1')}</li>`).join('')}</ul>
  </div>
  <div style="position:absolute;right:56px;top:50%;transform:translateY(-50%);width:${artWidth}px">${art}</div>`);

// An exact region (x, y, w, h in source pixels, 1920x1080 by default) of a capture, scaled to W pixels wide.
const region = (file, x, y, w, h, W, full = 1920) => `<div style="position:relative;width:${W}px;height:${Math.round(W * h / w)}px;overflow:hidden">
    <img src="${shot(file)}" style="position:absolute;max-width:none;width:${W * full / w}px;left:${-x * W / w}px;top:${-y * W / w}px"></div>`;
const framed = (inner, style = '') => `<div class="frame" style="${style}">${inner}</div>`;
const label = (text, style, small = '') => `<div class="label" style="${style}">${text}${small ? `<small>${small}</small>` : ''}</div>`;
const stats = (items) => `<div class="stats">${items.map(([t, v, flex]) =>
  `<div${flex ? ` style="flex:${flex}"` : ''}><div class="v">${v}</div><div class="t">${t}</div></div>`).join('')}</div>`;
// Two overlapping captures, the first top left and the second bottom right.
const pair = (first, second, firstLabel, secondLabel, height = 640, width = '73%') => `<div style="position:relative;height:${height}px">
    <div class="frame" style="position:absolute;left:0;top:0;width:${width}">${first}</div>
    <div class="frame" style="position:absolute;right:0;bottom:0;width:${width}">${second}</div>
    ${label(firstLabel, 'left:24px;top:22px')}${label(secondLabel, 'right:24px;bottom:22px')}</div>`;
// Two captures side by side, each with a label in its top left corner.
const duo = (first, second, firstLabel, secondLabel) => `<div style="display:grid;grid-template-columns:1fr 1fr;gap:18px">
    <div style="position:relative">${framed(first)}${label(firstLabel, 'left:20px;top:18px')}</div>
    <div style="position:relative">${framed(second)}${label(secondLabel, 'left:20px;top:18px')}</div></div>`;
const img = (file) => `<img src="${shot(file)}" style="display:block;width:100%">`;
const code = (file, body) => `<div class="frame" style="background:rgba(255,255,255,.96);padding:24px 34px">
  <div style="font-family:BlText;font-weight:800;font-size:18px;letter-spacing:.08em;color:var(--dim);margin-bottom:10px">${file}</div><pre class="code">${body}</pre></div>`;
const k = (t) => `<span style="color:#d0578a">${t}</span>`;
const v = (t) => `<span style="color:#7a5bd0">${t}</span>`;
const cm = (t) => `<span style="color:#b49bb8">${t}</span>`;

const versionText = VERSIONS.map((ver) => ver.minecraft).join(' · ');

// A row of colour chips: [name, colour, caption].
const swatches = (list, size = 46) => `<div class="items">${list.map(([name, color, caption]) => `<div>
  <div style="width:${size}px;height:${size}px;margin:2px auto 0;border-radius:50%;background:${color};box-shadow:0 0 0 3px #fff,0 0 0 5px rgba(226,100,143,.35)"></div>
  <div class="n" style="margin-top:9px">${name}</div><div class="t" style="font-size:14px;color:var(--dim)">${caption}</div></div>`).join('')}</div>`;

const assets = {
  banner: () => page(1800, 600, `
    <img src="${shot('princess-bedroom.jpg')}" style="position:absolute;right:-60px;top:-170px;width:1320px;-webkit-mask-image:linear-gradient(90deg,transparent 0%,#000 30%);mask-image:linear-gradient(90deg,transparent 0%,#000 30%)">
    <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(255,246,250,.98) 0%,rgba(255,246,250,.94) 32%,rgba(255,246,250,.4) 54%,rgba(255,246,250,0) 72%)"></div>
    <div style="position:absolute;inset:0;background:linear-gradient(0deg,rgba(255,240,247,.55) 0%,rgba(255,240,247,0) 26%)"></div>
    ${motes(48, 11)}
    <div style="position:absolute;left:86px;top:52px;width:1000px">
      <div class="kicker"><span class="heart"></span>${RELEASE.version} · ${RELEASE.name}</div>
      <div class="display candy soft" style="font-size:116px;line-height:1;white-space:nowrap;margin:16px 0 4px">Coquette</div>
      <div class="display candy soft" style="font-size:82px;line-height:1;white-space:nowrap">Furniture</div>
      <div class="rule" style="width:620px;margin:20px 0 22px"></div>
      <div style="font-family:BlDisplay;font-size:40px;color:var(--plum);line-height:1.2">Dream bedrooms and pastel home decor.</div>
      <div style="font-size:27px;color:var(--text);line-height:1.35;margin-top:8px">37 pieces in six pastel colours, plants and<br>decorations: 239 blocks that sit, sleep, store and cook.</div>
      <div style="margin-top:26px">${LOADERS.map((l) => `<span class="pill">${l}</span>`).join('')}<span class="pill ghost">No dependencies</span></div>
      <div style="margin-top:14px;font-family:BlDisplay;font-size:26px;color:var(--dim)">Minecraft ${versionText}</div>
    </div>`),

  icon: () => page(512, 512, `
    <div style="position:absolute;inset:0;background:radial-gradient(circle at 50% 40%,#fff6fa 0%,#fbe0ee 55%,#e6dcff 100%)"></div>${motes(26, 5)}
    <div style="position:absolute;left:52px;top:52px;width:408px;height:408px;border-radius:50%;overflow:hidden;background:#fff;
                box-shadow:0 0 0 6px #fff,0 0 0 10px #f59cbc,0 0 0 14px #fff,0 0 0 17px #c3adf3,0 24px 50px rgba(160,80,140,.35)">
      <img src="${source('logo.png')}" style="position:absolute;left:-24px;top:4px;width:816px;height:408px;image-rendering:pixelated;max-width:none"></div>
    ${[[256, 30], [482, 256], [256, 482], [30, 256]].map(([x, y], i) => `<div style="position:absolute;left:${x - 17}px;top:${y - 15}px">${heartSvg(i % 2 ? '#b7a2ee' : '#f59cbc', 34)}</div>`).join('')}`),

  'card-rooms': () => card({
    kicker: '239 blocks', title: 'Five rooms<br>to furnish',
    points: ['Bedroom, living room, kitchen, bathroom and garden', '37 pieces, each in six pastel colours',
      'Four potted plants and thirteen decorations', 'Cabriole legs, gold knobs, bows, lace and hearts', 'Most of them do something'],
    art: `<div style="display:grid;grid-template-columns:1fr 1fr;gap:18px">${[['princess-bedroom.jpg', 'Bedroom'], ['angelcore-living-room.jpg', 'Living room'],
      ['pastel-kitchen.jpg', 'Kitchen'], ['lavender-dreams.jpg', 'Bath corner']].map(([file, name]) => `<div style="position:relative">${framed(region(file, 330, 300, 1500, 780, 467))}
      ${label(name, 'left:18px;bottom:16px;font-size:21px')}</div>`).join('')}</div>` }),

  'card-bedroom': () => card({
    kicker: 'Bedroom', title: 'Sweet<br>dreams',
    points: ['A real canopy bed: sleep and set your spawn', 'Wardrobes, dressers and drawers that open up',
      'Pamper yourself at the vanity for Regeneration', 'A jewelry box with a dancing ballerina', 'Lamps, pillows and a cheval mirror'],
    art: pair(region('princess-bedroom.jpg', 330, 300, 1500, 780, 715), region('open-storage.jpg', 120, 100, 1680, 880, 715),
      'Princess Bedroom', 'Doors and drawers open', 600) +
      stats([['Wardrobe', '54 slots'], ['Dresser', '27 slots'], ['Jewelry box', '5 slots'], ['Night lamp', 'light 14']]) }),

  'card-living-room': () => card({
    kicker: 'Living room', title: 'Tea for<br>two',
    points: ['Sofas side by side join into one long sofa', 'Rugs join into one big rug with a single border',
      'Bookshelves power enchanting tables', 'A display cabinet shows off four items', 'Curtains tie open with bows'],
    art: framed(region('decor.jpg', 0, 0, 1920, 1080, 978)) +
      stats([['Sit on', 'sofas & armchairs', 1.5], ['Display', '4 items'], ['Bookshelf', '+1 power']]) }),

  'card-kitchen': () => card({
    kicker: 'Kitchen', title: 'Pastel<br>kitchen',
    points: ['The stove is a real smoker that glows while it cooks', 'Endless water from the sink for buckets and bottles',
      'A retro fridge full of cake, milk and strawberries', 'Dining tables join into long or square tables', 'Pour a cup of strawberry tea'],
    art: framed(region('pastel-kitchen.jpg', 240, 300, 1680, 780, 978)) +
      stats([['Fridge', '36 slots'], ['Stove', '2× speed'], ['Sink', 'endless water', 1.4]]) }),

  'card-bathroom': () => card({
    kicker: 'Bathroom', title: 'Bubble<br>bath',
    points: ['Fill the clawfoot tub with a water bucket', 'Sit in it for a bubble bath and slow Regeneration',
      'Pedestal sinks with running taps', 'A mirrored cabinet, towel rack and soap dispenser', 'There is a rubber duck'],
    art: framed(region('lavender-dreams.jpg', 560, 330, 1360, 750, 978)) }),

  'card-garden': () => card({
    kicker: 'Garden', title: 'Strawberry<br>cottage',
    points: ['Picket fences that join wooden fences and gates', 'A swing seat on flower-wrapped ropes',
      'Benches, a bistro table and gingham picnic blankets', 'A birdhouse with a bluebird that chirps by day', 'Pastel flower pots'],
    art: framed(region('strawberry-cottage.jpg', 120, 380, 1700, 700, 978)) }),

  'card-decor': () => card({
    kicker: 'Decorations', title: 'Little<br>touches',
    points: ['Thirteen decorations and four potted plants', 'They sink onto tables, shelves, sofas and beds',
      'Light the pastel candles with flint and steel', 'Stack books and pillows up to three high', 'A heart music box that plays a tune'],
    art: pair(region('decor.jpg', 200, 250, 1500, 830, 715), region('night.jpg', 300, 180, 1400, 780, 715),
      'Sits on the coffee table', 'Candles and lamps', 640) }),

  'card-night': () => card({
    kicker: 'After dark', title: 'Cosy at<br>night',
    points: ['Night lamps switch on and off', 'Pastel candles light up and snuff out', 'The stove glows while it cooks',
      'Sleep in the canopy bed to skip the night'],
    art: framed(region('night.jpg', 160, 100, 1600, 900, 978)) +
      stats([['Night lamp', 'light 14'], ['Candles', 'light 12'], ['Stove', 'light 13']]) }),

  'card-catalog': () => card({
    kicker: 'Interior Designer Mode', title: 'The design<br>catalog',
    points: ['Browse by room or by set, search and filter by colour', 'A big 3D preview of every piece',
      'What it does, its sets and its live recipe', 'Take pieces straight to your inventory in creative', 'Open it with the catalog or press K'],
    art: pair(region('catalog.jpg', 170, 30, 1590, 1020, 715), region('catalog-kitchen.jpg', 170, 30, 1590, 1020, 715),
      'Bedroom', 'Kitchen', 700, '70%') }),

  'card-sets': () => card({
    kicker: 'Furniture sets', title: 'Five themed<br>sets',
    points: ['Princess Bedroom and Lavender Dreams', 'Strawberry Cottage and the Pastel Kitchen', 'The Angelcore Room',
      'Take a whole set at once in creative', 'Tooltips show which sets a piece is in'],
    art: framed(region('catalog-sets.jpg', 170, 30, 1590, 1020, 978)) +
      swatches(SETS.map(([name, color, n]) => [name, color, `${n} pieces`]), 40) }),

  'card-colours': () => card({
    kicker: 'Six colourways', title: 'Paint it<br>pastel',
    points: ['Every piece of furniture in six colours', 'Right-click a placed piece with a dye to repaint it',
      'Both halves change together and storage keeps its contents', 'Or recolour any piece in the crafting grid'],
    art: framed(region('colours.jpg', 360, 320, 1200, 680, 978)) +
      swatches(COLOURS.map(([name, color, dye]) => [name, color, dye])) }),

  'card-config': () => card({
    kicker: 'Make it yours', title: 'Tune every<br>feature',
    points: ['Turn seating, recolouring or endless water off', 'Change the vanity cooldown', 'Allow catalog delivery in survival',
      'Same files on every loader and version'],
    art: `<div style="display:flex;flex-direction:column;gap:24px">
      ${code('config/coquettefurniture-common.toml', `${k('[furniture]')}
  ${k('seating')} = ${v('true')}
  ${k('dyeRecoloring')} = ${v('true')}
  ${k('sinksGiveWater')} = ${v('true')}
  ${k('vanityCooldownSeconds')} = ${v('120')}  ${cm('# 0 ~ 3600')}
${k('[catalog]')}
  ${k('deliveryInSurvival')} = ${v('false')}
  ${k('giveCatalogOnFirstJoin')} = ${v('true')}`)}
      ${code('config/coquettefurniture-client.toml', `${k('[display]')}
  ${k('featureTooltips')} = ${v('true')}
  ${k('displayCabinetSpin')} = ${v('true')}`)}</div>` }),
};

const headers = { 'header-furniture': 'The Furniture', 'header-bedroom': 'Bedroom', 'header-living-room': 'Living Room',
  'header-kitchen': 'Kitchen', 'header-bathroom': 'Bathroom', 'header-garden': 'Garden', 'header-decorations': 'Decorations',
  'header-catalog': 'Interior Designer Mode', 'header-sets': 'Furniture Sets', 'header-colours': 'Six Colourways',
  'header-recipes': 'Recipes', 'header-config': 'Configuration', 'header-everything': 'Everything Else',
  'header-compatibility': 'Downloads & Compatibility', 'header-faq': 'Questions', 'header-gallery': 'Gallery' };
for (const [name, title] of Object.entries(headers)) assets[name] = () => header(title);
assets.divider = () => page(1400, 40, `<div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:14px">
  <div class="rule" style="width:480px;-webkit-mask-image:linear-gradient(90deg,transparent,#000);mask-image:linear-gradient(90deg,transparent,#000)"></div>
  ${heartSvg('#b7a2ee', 20)}${heartSvg('#f59cbc', 26)}${heartSvg('#93dcc2', 20)}
  <div class="rule" style="width:480px;-webkit-mask-image:linear-gradient(90deg,#000,transparent);mask-image:linear-gradient(90deg,#000,transparent)"></div></div>`, true);


(async () => {
  const wanted = process.argv.slice(2);
  const browser = await chromium.launch();
  fs.mkdirSync(out, { recursive: true });
  for (const [name, build] of Object.entries(assets)) {
    if (wanted.length && !wanted.includes(name)) continue;
    const html = build();
    const size = html.match(/body \{ width: (\d+)px; height: (\d+)px; \}/);
    const tab = await browser.newPage({ viewport: { width: +size[1], height: +size[2] }, deviceScaleFactor: 1 });
    const file = path.join(__dirname, `.render-${name}.html`);
    fs.writeFileSync(file, html);
    await tab.goto(url(file));
    await tab.evaluate(() => document.fonts.ready);
    await tab.waitForLoadState('networkidle');
    await tab.screenshot({ path: path.join(out, `${name}.png`), omitBackground: name === 'divider' || name.startsWith('header-') });
    fs.unlinkSync(file);
    await tab.close();
    console.log('rendered', name);
  }
  await browser.close();
})().catch((error) => { console.error(error); process.exit(1); });
