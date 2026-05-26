# 📸 TEST EVIDENCE COLLECTION GUIDE

**Purpose:** Gather all test execution outputs, screenshots, and logs for the PDF regression report

---

## 🎯 QUICK START

**Total Time Required:** 30-45 minutes  
**Main Artifacts to Collect:** 8-10 screenshots + 3-4 log files

---

## ✅ STEP 1: Compile Backend Success (5 minutes)

### Command
```bash
cd backend
mvn clean compile
```

### Expected Output
```
[INFO] BUILD SUCCESS
[INFO] Total time: X.XXs
[INFO] Finished at: [date/time]
```

### Screenshot to Capture
- Terminal showing "BUILD SUCCESS" at the end
- **Filename:** `01-mvn-compile-success.png`

### Screenshot Tips
- **On Windows:** Press `Shift + Windows + S` to capture screen area
- **Save to:** `docs/TestEvidence/Screenshots/`

---

## ✅ STEP 2: Run Backend Tests (5 minutes)

### Command
```bash
cd backend
mvn test 2>&1 | tail -100
```

### Expected Output
```
[INFO] Tests run: 15
[INFO] Failures: X
[INFO] Errors: X
[INFO] BUILD SUCCESS
```

### Captures to Make

**Screenshot #1: Test Execution Completion**
- **Content:** Final summary showing tests run, failures, errors
- **Filename:** `02-mvn-test-summary.png`
- **Command Alternative:** `mvn test 2>&1 | Select-Object -Last 50` (PowerShell)

**Screenshot #2: AuthServiceTest Passing**
- **Content:** Terminal output showing "7/7 PASSED" or similar
- **Filename:** `03-authservice-tests-passing.png`
- **Extract From:** Maven test log

**Screenshot #3: Full Build Success**
- **Content:** "BUILD SUCCESS" at end of Maven output
- **Filename:** `04-build-success.png`

### Log Files to Collect

**Save Console Output:**
```bash
mvn test > test-output.txt 2>&1
```
- **Filename:** `test-output.txt`
- **Location:** `docs/TestEvidence/Logs/`

**Copy Surefire Reports:**
```bash
dir backend\target\surefire-reports\
copy backend\target\surefire-reports\*.txt docs\TestEvidence\Reports\
copy backend\target\surefire-reports\*.xml docs\TestEvidence\Reports\
```
- **Files to copy:**
  - `AuthServiceTest.txt`
  - `BorrowServiceTest.txt`
  - `TEST-*.xml` files

---

## ✅ STEP 3: Mobile Build Verification (3 minutes)

### Command (Windows)
```bash
cd mobile
.\gradlew.bat build --dry-run 2>&1 | tail -50
```

### Expected Output
```
BUILD SUCCESSFUL in X seconds
[List of skipped tasks...]
```

### Screenshot to Capture
- **Content:** "BUILD SUCCESSFUL" message
- **Filename:** `05-mobile-gradle-success.png`

### Save Log
```bash
cd mobile
.\gradlew.bat build --dry-run > gradle-build.log 2>&1
copy gradle-build.log ..\docs\TestEvidence\Logs\
```

---

## ✅ STEP 4: Project Structure Screenshots (10 minutes)

Use VS Code Explorer to show organized features:

### Screenshot #1: Backend Features Structure
- **What to show:** `backend/src/main/java/.../features/`
- **Include:** auth, assets, borrow, admin, shared folders
- **Filename:** `06-backend-features-structure.png`
- **Steps:**
  1. Open VS Code Explorer
  2. Expand `backend/src/main/java/.../features/`
  3. Show all feature folders
  4. Screenshot

### Screenshot #2: Web Features Structure
- **What to show:** `web/src/features/`
- **Include:** auth, assets, borrow, admin, shared folders
- **Filename:** `07-web-features-structure.png`

### Screenshot #3: Mobile Features Structure
- **What to show:** `mobile/app/src/main/java/assetflow/features/`
- **Include:** auth, assets, borrow with new package structure
- **Filename:** `08-mobile-features-structure.png`

---

## ✅ STEP 5: Git History Screenshots (5 minutes)

### Command
```bash
git log --oneline refactor/vertical-slice-architecture | head -20
```

### Screenshot
- **Content:** 14+ commits showing refactoring work
- **Filename:** `09-git-commit-history.png`

### Example of Well-Organized Git Log
```
2199529 docs: add session summary with phase 3 achievements
b11edf7 docs: create comprehensive progress report
c880403 test: add lenient Mockito strictness to BorrowServiceTest
6ed2d30 test: add lenient Mockito strictness to AuthServiceTest
edd7fdb test: fix Mockito stubbing to handle both generateToken overloads
09ff079 test: create...
```

### Additional Info to Document
```bash
git branch -v  # Show all branches
git status     # Show current status
git log --oneline | wc -l  # Show total commits
```

---

## ✅ STEP 6: Organize Evidence Files

### Create Directory Structure
```bash
mkdir -p docs/TestEvidence/Screenshots
mkdir -p docs/TestEvidence/Logs
mkdir -p docs/TestEvidence/Reports
```

### Final Organization
```
docs/TestEvidence/
├── Screenshots/
│   ├── 01-mvn-compile-success.png
│   ├── 02-mvn-test-summary.png
│   ├── 03-authservice-tests-passing.png
│   ├── 04-build-success.png
│   ├── 05-mobile-gradle-success.png
│   ├── 06-backend-features-structure.png
│   ├── 07-web-features-structure.png
│   ├── 08-mobile-features-structure.png
│   └── 09-git-commit-history.png
├── Logs/
│   ├── test-output.txt
│   └── gradle-build.log
└── Reports/
    ├── AuthServiceTest.txt
    ├── BorrowServiceTest.txt
    ├── TEST-AuthServiceTest.xml
    └── TEST-BorrowServiceTest.xml
```

---

## 📋 EVIDENCE CHECKLIST

### Screenshots (9 total)
- ☑ MVP compile success
- ☑ Test execution summary
- ☑ Auth tests passing
- ☑ Build success message
- ☑ Mobile gradle build
- ☑ Backend features structure
- ☑ Web features structure
- ☑ Mobile features structure
- ☑ Git commit history

### Log Files (4 total)
- ☑ `test-output.txt` (full Maven test log)
- ☑ `gradle-build.log` (Gradle build output)
- ☑ `AuthServiceTest.txt` (Surefire report)
- ☑ `BorrowServiceTest.txt` (Surefire report)

### Report Files (XML - optional)
- ☑ `TEST-AuthServiceTest.xml`
- ☑ `TEST-BorrowServiceTest.xml`

---

## 🎨 SCREENSHOT TIPS

### Better Screenshots
1. **Clean up terminal** before capturing
   ```bash
   clear  # On Mac/Linux
   cls    # On Windows CMD
   ```

2. **Zoom in for readability**
   - Increase terminal font size to 14-16pt
   - Take screenshot
   - Reset font

3. **Crop important parts**
   - Use Paint or online editor
   - Remove unnecessary whitespace
   - Keep only relevant content

4. **Add annotations** (optional)
   - Highlight key lines (BUILD SUCCESS, Tests run, PASSED)
   - Use arrows to point out important info

### Using VS Code for Screenshots
1. **File → Preferences → Settings**
2. Search: `fontSize`
3. Temporarily increase to 16-18pt
4. Take screenshot
5. Reset to normal size

---

## 📄 CONVERTING SCREENSHOTS TO PDF (Optional)

If you want to create a quick evidence PDF:

### Using Microsoft Word
1. Open Word
2. Insert → Pictures → From this device
3. Select all screenshots in order
4. Add title/labels if desired
5. File → Export as PDF

### Using Google Docs
1. Create new Google Doc
2. Insert → Image → Upload from computer
3. Add screenshots in order
4. File → Download → PDF Document

### Using Python (Advanced)
```python
from PIL import Image
from PyPDF2 import PdfMerger
import os

# Resize images for PDF (optional)
# Then convert images to PDF
images = []
for img_file in sorted(os.listdir('Screenshots')):
    img = Image.open(f'Screenshots/{img_file}')
    images.append(img.convert('RGB'))

images[0].save('TestEvidence.pdf', save_all=True, append_images=images[1:])
```

---

## ⏱️ TIME TRACKING

| Task | Time | Status |
|------|------|--------|
| Backend compile | 5 min | ⏳ |
| Backend tests | 5 min | ⏳ |
| Mobile build | 3 min | ⏳ |
| Screenshots | 10 min | ⏳ |
| Git history | 5 min | ⏳ |
| Organize files | 5 min | ⏳ |
| **TOTAL** | **~33 min** | ⏳ |

---

## ✅ FINAL CHECKLIST

Before adding evidence to your PDF report:

- [ ] All 9 screenshots captured
- [ ] All 4 log files collected
- [ ] Files organized in `TestEvidence/` folder
- [ ] Screenshots renamed consistently (01-, 02-, etc.)
- [ ] Log files readable and complete
- [ ] Backup copies saved locally
- [ ] Ready to insert into PDF report

---

## 📥 EMBEDDING IN PDF REPORT

### In Microsoft Word
1. Put cursor where you want image
2. Insert → Pictures → From this device
3. Select image
4. Adjust size (right-click → Size and Position)
5. Add caption: Insert → Caption

### In PDF Template
```markdown
## 6. AUTOMATED TEST EXECUTION RESULTS

### Build Verification
![Maven Compile Success](TestEvidence/Screenshots/01-mvn-compile-success.png)
**Figure 1:** Maven compile successful, 27 .class files generated

### Test Execution
![Test Summary](TestEvidence/Screenshots/02-mvn-test-summary.png)
**Figure 2:** AuthServiceTest 7/7 passing

[Continue with other images...]
```

---

**READY TO COLLECT EVIDENCE!** 🚀

Run the commands in sequence, capture the screenshots, save the logs, and you'll have everything needed for a professional regression test report.
