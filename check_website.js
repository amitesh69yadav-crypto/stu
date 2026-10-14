const fs = require('fs');
const html = fs.readFileSync('index.html', 'utf8');

const eventRegex = /on(?:click|submit|change|input)\s*=\s*["']([^"']+)["']/gi;
let match;
const calledFuncs = new Set();
while ((match = eventRegex.exec(html)) !== null) {
  const expr = match[1];
  const fnMatches = expr.match(/[a-zA-Z0-9_$]+(?=\s*\()/g);
  if (fnMatches) {
    fnMatches.forEach(f => calledFuncs.add(f));
  }
}

const scriptMatches = [...html.matchAll(/<script>([\s\S]*?)<\/script>/gi)];
const scriptContent = scriptMatches.map(m => m[1]).join('\n');

const standardGlobals = new Set(['confirm', 'alert', 'prompt', 'encodeURIComponent', 'decodeURIComponent', 'parseInt', 'parseFloat', 'Boolean', 'String', 'Number', 'Date', 'Math', 'JSON', 'console', 'document', 'window', 'event']);

const missing = [];
for (const fn of calledFuncs) {
  if (standardGlobals.has(fn)) continue;
  const defRegex = new RegExp(`(?:function\\s+${fn}\\b|\\b${fn}\\s*=\\s*)`);
  if (!defRegex.test(scriptContent)) {
    missing.push(fn);
  }
}

console.log('Total handler functions found:', calledFuncs.size);
console.log('Missing functions:', missing);
