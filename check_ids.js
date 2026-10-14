const fs = require('fs');
const html = fs.readFileSync('index.html', 'utf8');

const idGetRegex = /document\.getElementById\(['"]([^'"]+)['"]\)/g;
let match;
const queriedIds = new Set();
while ((match = idGetRegex.exec(html)) !== null) {
  queriedIds.add(match[1]);
}

const missingIds = [];
for (const id of queriedIds) {
  const hasId = new RegExp(`id=['"]${id}['"]`).test(html);
  if (!hasId) {
    missingIds.push(id);
  }
}

console.log('Queried IDs count:', queriedIds.size);
console.log('Missing DOM IDs:', missingIds);
