# Manual Regression Test Script - AssetFlow
**Date:** May 8, 2026  
**Tester:** [QA Name]  
**Environment:** Local Dev (Backend 8080, Web 5173, Mobile Emulator)  
**Duration:** ~2 hours  

---

## Session 1: Authentication & Authorization (30 min) -

### Test 1.1: Register New Student User  
**TC ID:** TC1.1  
**Objective:** Verify user registration with unique email and password hashing

**Steps:**
1. Open web app → Click "Register"
2. Fill form:
   - Email: `newstudent1@college.edu`
   - Name: `New Student`
   - Password: `TestPass@123`
   - Confirm: `TestPass@123`
3. Click "Register"

**Expected Result:**
- ✓ Form validates (no errors)
- ✓ Registration succeeds
- ✓ Redirected to login page or dashboard
- ✓ User appears in backend database with hashed password (check via SQL)

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

### Test 1.2: Prevent Duplicate Email Registration  
**TC ID:** TC1.2

**Steps:**
1. Try to register with existing email: `student@test.edu`
2. Leave other fields blank/same
3. Click "Register"

**Expected Result:**
- ✓ Error message appears: "Email already registered"
- ✓ User stays on register page
- ✓ No duplicate user created in database

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

### Test 1.3: Login as Student  
**TC ID:** TC1.4

**Steps:**
1. Go to login page
2. Enter:
   - Email: `student@test.edu`
   - Password: `TestPass@123` (or correct password)
3. Click "Login"

**Expected Result:**
- ✓ Login succeeds
- ✓ Redirected to dashboard
- ✓ User name/email displayed in header
- ✓ JWT token stored in localStorage (check DevTools Console → `localStorage.getItem('token')`)
- ✓ No admin controls visible (e.g., no "Manage Requests" button if user is not admin)

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

### Test 1.4: Login as Admin  
**TC ID:** TC1.5

**Steps:**
1. Go to login page
2. Enter admin credentials:
   - Email: `admin@admin.com` (or test admin email)
   - Password: `AdminPass@123` (correct admin password)
3. Click "Login"

**Expected Result:**
- ✓ Login succeeds
- ✓ Dashboard shows admin panel/controls
- ✓ "Manage Borrow Requests" button visible
- ✓ "View Statistics" or admin metrics visible

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

### Test 1.5: Logout  
**TC ID:** TC1.10 (Logout is optional but recommended)

**Steps:**
1. While logged in, click "Logout" button (top-right or menu)
2. Verify redirect to login page

**Expected Result:**
- ✓ Logged out successfully
- ✓ Redirected to login/home page
- ✓ localStorage token cleared (verify in DevTools)

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

## Session 2: Asset Management (30 min)

### Test 2.1: View All Assets  
**TC ID:** TC2.1

**Steps:**
1. Login as student
2. Navigate to Dashboard
3. Observe asset list

**Expected Result:**
- ✓ Asset list displays (at least 5+ assets)
- ✓ Each asset shows: Name, Serial Number, Category, Status badge
- ✓ Status badges show correct colors (Green=Available, Red=Borrowed, Yellow=Maintenance)
- ✓ No errors in console (check DevTools)

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

### Test 2.2: Filter Assets by Status  
**TC ID:** TC2.2

**Steps:**
1. On Dashboard, find Status filter dropdown
2. Select "Available"
3. Observe list updated

**Expected Result:**
- ✓ Only AVAILABLE assets display
- ✓ All other statuses disappear
- ✓ Count shows filtered results

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

### Test 2.3: Filter Assets by Category  
**TC ID:** TC2.3

**Steps:**
1. Find Category filter
2. Select "Laboratory Equipment" (or any category)
3. Observe list

**Expected Result:**
- ✓ Only assets of selected category display
- ✓ Other categories disappear

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

### Test 2.4: View Asset Detail  
**TC ID:** TC2.4

**Steps:**
1. Click on an asset card (e.g., "Microscope")
2. Navigate to detail page

**Expected Result:**
- ✓ Page loads with full asset info:
  - Name, Serial Number, Category, Status, Description
  - Asset image (if uploaded)
  - "Borrow" button (if available)
  - Related transaction history (if any)
- ✓ No console errors

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

## Session 3: Borrow Transaction System (45 min)

### Test 3.1: Submit Borrow Request  
**TC ID:** TC3.1

**Steps:**
1. Login as student
2. Go to an Asset with status="AVAILABLE"
3. Click "Borrow" button
4. Fill form:
   - Due Date: Select date 7 days from today (`calendar picker`)
5. Click "Submit Request"

**Expected Result:**
- ✓ Request submitted successfully
- ✓ Message: "Request submitted" or similar
- ✓ Request appears in "My Transactions" page
- ✓ Request status shows "PENDING"
- ✓ Asset status changes to "BORROWED" on dashboard

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

### Test 3.2: View Request in "My Transactions"  
**TC ID:** TC3.8

**Steps:**
1. From dashboard, click "My Transactions" (or similar menu)
2. View list of your borrow requests

**Expected Result:**
- ✓ Newly submitted request appears
- ✓ Shows: Asset Name, Due Date, Status (PENDING)
- ✓ Only YOUR requests shown (not others')
- ✓ No other student's transactions visible

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

### Test 3.3: Admin Approves Borrow Request  
**TC ID:** TC3.4

**Steps:**
1. Logout as student
2. Login as admin
3. Navigate to "Manage Borrow Requests" (or admin panel)
4. Find the pending request from Test 3.1
5. Click "Approve"

**Expected Result:**
- ✓ Request status changes to "APPROVED"
- ✓ Success message displayed
- ✓ Asset status on dashboard shows "BORROWED"
- ✓ Student can view approval in their "My Transactions"

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

### Test 3.4: Reject Borrow Request  
**TC ID:** TC3.6

**Steps:**
1. As admin, create another pending request (or find one)
2. Click "Reject" on the request

**Expected Result:**
- ✓ Request status changes to "REJECTED"
- ✓ Asset status remains "AVAILABLE"
- ✓ Student is notified (via email or in-app message)

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

### Test 3.5: Return Borrowed Asset  
**TC ID:** TC3.7

**Steps:**
1. As admin, find an APPROVED (currently BORROWED) request
2. Click "Return Asset"
3. Confirm return

**Expected Result:**
- ✓ Request status changes to "RETURNED"
- ✓ Return date is set to today
- ✓ Asset status changes back to "AVAILABLE"
- ✓ Message: "Asset returned successfully"

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

## Session 4: Authorization & Security (15 min)

### Test 4.1: Student Cannot Access Admin Endpoints  
**TC ID:** TC4.2

**Steps:**
1. Login as student JWT token
2. Try to access: `http://localhost:8080/api/admin/stats` (or any admin endpoint)
   - Option A: Direct URL access
   - Option B: Check network tab in DevTools when admin clicks buttons

**Expected Result:**
- ✓ HTTP 403 Forbidden error OR
- ✓ Page not found / redirected to dashboard
- ✓ No admin data exposed to student

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

### Test 4.2: Student UI Has No Admin Controls  
**TC ID:** TC4.2

**Steps:**
1. Login as student
2. Use browser DevTools → Inspect Element
3. Search for admin-only buttons like "Manage," "Approve," "Reject"

**Expected Result:**
- ✓ No admin buttons found in DOM
- ✓ Sidebar/menu shows only student options
- ✓ Admin pages (if URL-hacked) show "Unauthorized" or redirect

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

### Test 4.3: Admin Sees All Requests  
**TC ID:** TC3.9

**Steps:**
1. Login as admin
2. Go to "Manage Borrow Requests"

**Expected Result:**
- ✓ All requests from ALL students visible (not just your own)
- ✓ Can filter/search by student name or asset
- ✓ Shows summary statistics (pending, approved, returned)

**Actual Result:** [________________]  
**Status:** ☐ PASS ☐ FAIL  
**Notes:** [________________]

---

## Summary

**Total Test Cases:** 13  
**Passed:** _____ / 13  
**Failed:** _____ / 13  
**Pass Rate:** _____%  

### Failed Tests Details
(List any failures and next steps)
1. [___________________] → Root Cause: [_______________]
2. [___________________] → Root Cause: [_______________]

### Regression Issues Found
(Major issues that don't match expected behavior)
- Issue 1: [_______________]
- Issue 2: [_______________]

---

**Tester Sign-Off:** _________________________  
**Date:** ______________  
**Time Spent:** __________min

