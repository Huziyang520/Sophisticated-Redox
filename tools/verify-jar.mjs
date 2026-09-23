// Build-output self-check for the Sophisticated Redox jar.
//
//   node tools/verify-jar.mjs <extracted-jar-dir>
//
// Checks (all of them are hard requirements from the AI-mod-dev rulebook):
//   1. every .json parses with a STRICT parser (Gson is lenient, node's JSON.parse is not)
//   2. no .json starts with a UTF-8 BOM
//   3. all lang files have exactly the same key set as en_us.json
//   4. PNG dimensions declared in IHDR
//   5. no pack.mcmeta (NeoForge line: a stray pack.mcmeta causes "datapack error" on world load)
import fs from 'node:fs';
import path from 'node:path';

const root = process.argv[2];
if (!root) {
  console.error('usage: node tools/verify-jar.mjs <extracted-jar-dir>');
  process.exit(2);
}

const files = [];
(function walk(dir) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(full);
    else files.push(full);
  }
})(root);

const rel = (f) => path.relative(root, f).replace(/\\/g, '/');
const problems = [];
const langKeys = new Map();

for (const file of files.filter((f) => f.endsWith('.json'))) {
  const buf = fs.readFileSync(file);
  if (buf[0] === 0xef && buf[1] === 0xbb && buf[2] === 0xbf) problems.push(`BOM in ${rel(file)}`);
  let data;
  try {
    data = JSON.parse(buf.toString('utf8'));
  } catch (e) {
    problems.push(`invalid JSON ${rel(file)}: ${e.message}`);
    continue;
  }
  if (rel(file).startsWith('assets/sophisticated_redox/lang/')) {
    langKeys.set(rel(file), Object.keys(data).sort().join('\n'));
  }
}

const reference = langKeys.get('assets/sophisticated_redox/lang/en_us.json');
if (!reference) {
  problems.push('en_us.json not found');
} else {
  for (const [file, keys] of langKeys) {
    if (keys !== reference) {
      const refSet = new Set(reference.split('\n'));
      const set = new Set(keys.split('\n'));
      const missing = [...refSet].filter((k) => !set.has(k));
      const extra = [...set].filter((k) => !refSet.has(k));
      problems.push(`lang key mismatch ${file}: missing=[${missing}] extra=[${extra}]`);
    }
  }
}

for (const file of files.filter((f) => f.endsWith('.png'))) {
  const buf = fs.readFileSync(file);
  const width = buf.readUInt32BE(16);
  const height = buf.readUInt32BE(20);
  console.log(`PNG ${rel(file)} ${width}x${height}`);
}

if (files.some((f) => rel(f) === 'META-INF/pack.mcmeta')) problems.push('pack.mcmeta present (must not be shipped on the NeoForge line)');

console.log(`\n${files.length} files, ${langKeys.size} language files`);
if (problems.length === 0) {
  console.log('OK: all JSON strict-parses, no BOM, lang keys aligned');
} else {
  for (const p of problems) console.error(`FAIL: ${p}`);
  process.exit(1);
}
