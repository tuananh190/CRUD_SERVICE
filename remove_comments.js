const fs = require('fs');
const path = require('path');

const targetDir = path.join(__dirname, 'src', 'main', 'java');

function walkDir(dir, callback) {
    fs.readdirSync(dir).forEach(f => {
        let dirPath = path.join(dir, f);
        let isDirectory = fs.statSync(dirPath).isDirectory();
        isDirectory ? walkDir(dirPath, callback) : callback(path.join(dir, f));
    });
}

function removeComments(code) {
    // Regex matches string literals, char literals, multi-line comments, and single-line comments
    const regex = /("(?:\\[\s\S]|[^"])*")|('(?:\\[\s\S]|[^'])*')|(\/\*[\s\S]*?\*\/)|(\/\/.*)/g;
    
    let processed = code.replace(regex, (match, p1, p2, p3, p4) => {
        if (p1 || p2) {
            // Keep strings and chars
            return match;
        } else {
            // Remove comments
            return '';
        }
    });
    
    // Clean up empty lines
    const lines = processed.split(/\r?\n/);
    const cleanedLines = [];
    let previousLineEmpty = false;
    
    for (let line of lines) {
        if (line.trim().length === 0) {
            if (!previousLineEmpty) {
                // Keep at most one blank line for visual separation
                cleanedLines.push('');
                previousLineEmpty = true;
            }
        } else {
            // remove trailing spaces
            cleanedLines.push(line.replace(/[ \t]+$/, ''));
            previousLineEmpty = false;
        }
    }
    
    // Also remove leading empty lines
    while (cleanedLines.length > 0 && cleanedLines[0].trim().length === 0) {
        cleanedLines.shift();
    }
    
    return cleanedLines.join('\n');
}

let count = 0;
walkDir(targetDir, function(filePath) {
    if (filePath.endsWith('.java')) {
        let content = fs.readFileSync(filePath, 'utf8');
        let newContent = removeComments(content);
        if (content !== newContent) {
            fs.writeFileSync(filePath, newContent, 'utf8');
            count++;
        }
    }
});

console.log(`Successfully removed comments from ${count} Java files.`);
