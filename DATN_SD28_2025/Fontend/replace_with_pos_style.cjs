const fs = require('fs');
const path = require('path');

const viewsDir = path.join(__dirname, 'src', 'views');

// Files to update
const filesToUpdate = [
  'HoaDonPage.vue',
  'SanPhamPage.vue', 
  'NhanVienPage.vue',
  'VoucherPage.vue',
  'ThongKePage.vue',
  'OnlinePage.vue',
  'BaoCaoBanHangPage.vue',
  'BaoCaoTonKhoPage.vue',
  'HangPage.vue',
  'ManHinhPage.vue',
  'HeDieuHanhPage.vue',
  'MauSacPage.vue',
  'ChipPage.vue',
  'RamPage.vue',
  'RomPage.vue',
  'CpuPage.vue',
  'GpuPage.vue',
  'PinPage.vue',
  'CameraTruocPage.vue',
  'CameraSauPage.vue'
];

filesToUpdate.forEach(fileName => {
  const filePath = path.join(viewsDir, fileName);
  
  if (fs.existsSync(filePath)) {
    fs.readFile(filePath, 'utf8', (err, data) => {
      if (err) {
        console.error(`Error reading ${fileName}:`, err);
        return;
      }

      let updatedContent = data;
      let changed = false;

      // 1. Replace import statement
      if (updatedContent.includes("import PosHeader from")) {
        updatedContent = updatedContent.replace(
          /import PosHeader from ['"][^'"]*['"]/g,
          "import PosStyleHeader from '@/components/PosStyleHeader.vue'"
        );
        changed = true;
        console.log(`Updated import in ${fileName}`);
      }

      // 2. Replace component usage
      if (updatedContent.includes('<PosHeader />')) {
        updatedContent = updatedContent.replace(/<PosHeader \/>/g, '<PosStyleHeader />');
        changed = true;
        console.log(`Updated component usage in ${fileName}`);
      }

      // 3. Add padding-top to main container
      const paddingPatterns = [
        /\.page\s*{\s*([^}]*padding:\s*[^;]*);?\s*([^}]*)}/g,
        /\.content\s*{\s*([^}]*padding:\s*[^;]*);?\s*([^}]*)}/g,
        /\.customer-page\s*{\s*([^}]*padding:\s*[^;]*);?\s*([^}]*)}/g
      ];

      paddingPatterns.forEach(pattern => {
        if (pattern.test(updatedContent) && !updatedContent.includes('padding-top: 160px')) {
          updatedContent = updatedContent.replace(pattern, (match, before, after) => {
            if (!before.includes('padding-top')) {
              return match.replace('padding:', 'padding: 20px;\n  padding-top: 160px;');
            }
            return match;
          });
          changed = true;
          console.log(`Added padding-top to ${fileName}`);
        }
      });

      if (changed) {
        fs.writeFile(filePath, updatedContent, 'utf8', (err) => {
          if (err) {
            console.error(`Error writing ${fileName}:`, err);
          } else {
            console.log(`Successfully updated ${fileName}`);
          }
        });
      } else {
        console.log(`No changes needed for ${fileName}`);
      }
    });
  } else {
    console.log(`File ${fileName} not found`);
  }
});

console.log('Script completed!');






