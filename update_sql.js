const fs = require('fs');

function updateSqlFile(filePath) {
    let content = fs.readFileSync(filePath, 'utf-8');
    
    // Update ENUM definition
    content = content.replace(
        /ENUM\('STORAGE_DEVICE', 'NETWORK_DEVICE', 'ACCESSORY'\)/g,
        "ENUM('HDD', 'SSD', 'USB', 'NAS', 'TAPE', 'ENCLOSURE', 'MEMORY_CARD')"
    );

    // Naive replacements for seed data (just map them to valid new categories)
    // To be more precise, we could look at the product name, but mapping them to HDD/NAS/MEMORY_CARD is a good start.
    const lines = content.split('\n');
    const updatedLines = lines.map(line => {
        if (line.includes('INSERT INTO `Product`') || line.includes('VALUES') || (line.startsWith('(') && line.endsWith(',')) || (line.startsWith('(') && line.endsWith(';'))) {
            if (line.includes("'STORAGE_DEVICE'")) {
                if (line.toLowerCase().includes('ssd')) {
                    return line.replace(/'STORAGE_DEVICE'/g, "'SSD'");
                } else if (line.toLowerCase().includes('usb')) {
                    return line.replace(/'STORAGE_DEVICE'/g, "'USB'");
                } else {
                    return line.replace(/'STORAGE_DEVICE'/g, "'HDD'");
                }
            }
            if (line.includes("'NETWORK_DEVICE'")) {
                return line.replace(/'NETWORK_DEVICE'/g, "'NAS'");
            }
            if (line.includes("'ACCESSORY'")) {
                return line.replace(/'ACCESSORY'/g, "'MEMORY_CARD'");
            }
        }
        return line;
    });
    
    fs.writeFileSync(filePath, updatedLines.join('\n'));
    console.log(`Updated ${filePath}`);
}

updateSqlFile('sto.sql');
updateSqlFile('seed.sql');
