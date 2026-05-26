const API_BASE = '/api';

// ── UI Elements ──────────────────────────────────────────────
const authView        = document.getElementById('auth-view');
const dashboardView   = document.getElementById('dashboard-view');
const btnLoginTab     = document.getElementById('tab-login');
const btnRegisterTab  = document.getElementById('tab-register');
const registerFields  = document.getElementById('register-fields');
const authForm        = document.getElementById('auth-form');
const authError       = document.getElementById('auth-error');
const authSubmit      = document.getElementById('auth-submit');
const btnLogout       = document.getElementById('btn-logout');
const txForm          = document.getElementById('tx-form');
const txCategorySelect= document.getElementById('tx-category');
const transactionList = document.getElementById('transaction-list');
const elTotalBalance  = document.getElementById('total-balance');
const elMonthlyIncome = document.getElementById('monthly-income');
const elMonthlyExpense= document.getElementById('monthly-expense');

// Modal elements
const modalOverlay      = document.getElementById('modal-overlay');
const modalTitle        = document.getElementById('modal-title');
const modalSubtitle     = document.getElementById('modal-subtitle');
const modalCategoryGrid = document.getElementById('modal-category-grid');
const modalAmount       = document.getElementById('modal-amount');
const modalDate         = document.getElementById('modal-date');
const modalDesc         = document.getElementById('modal-desc');
const modalError        = document.getElementById('modal-error');
const modalSubmitBtn    = document.getElementById('modal-submit-btn');
const modalCloseBtn     = document.getElementById('modal-close-btn');

// Income & Expense cards
const cardIncome  = document.getElementById('card-income');
const cardExpense = document.getElementById('card-expense');

// ── App State ────────────────────────────────────────────────
let categories     = [];
let modalMode      = 'INCOME'; // 'INCOME' | 'EXPENSE'
let selectedCatName = null;

// ── Cache Helpers ────────────────────────────────────────────
const getCachedCategories  = () => { try { const c = sessionStorage.getItem('categories'); return c ? JSON.parse(c) : null; } catch { return null; } };
const setCachedCategories  = (c) => sessionStorage.setItem('categories', JSON.stringify(c));
const clearCachedCategories= () => sessionStorage.removeItem('categories');

// ── Bootstrap ────────────────────────────────────────────────
setDefaultDate();
checkSession();

// ── Event Listeners ──────────────────────────────────────────
btnLoginTab   .addEventListener('click', () => switchAuthMode('login'));
btnRegisterTab.addEventListener('click', () => switchAuthMode('register'));
authForm      .addEventListener('submit', handleAuth);
btnLogout     .addEventListener('click', handleLogout);
txForm        .addEventListener('submit', handleAddTransaction);

// Stat card → open modal
cardIncome .addEventListener('click', () => openModal('INCOME'));
cardExpense.addEventListener('click', () => openModal('EXPENSE'));

// Modal close
modalCloseBtn.addEventListener('click', closeModal);
modalOverlay .addEventListener('click', (e) => { if (e.target === modalOverlay) closeModal(); });
document.addEventListener('keydown', (e) => { if (e.key === 'Escape') closeModal(); });

// Modal submit
modalSubmitBtn.addEventListener('click', handleModalSubmit);

// ── Helpers ──────────────────────────────────────────────────
function setDefaultDate() {
  const today = new Date().toISOString().split('T')[0];
  document.getElementById('tx-date').value   = today;
  document.getElementById('modal-date').value = today;
}

function fmt(n) {
  return Number(n || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

async function getErrorMsg(res) {
  try { const d = await res.json(); return d.message || `Error ${res.status}`; }
  catch { return `HTTP Error ${res.status}`; }
}

// ── Auth Mode Switch ──────────────────────────────────────────
function switchAuthMode(mode) {
  authError.textContent = '';
  if (mode === 'login') {
    btnLoginTab.classList.add('active');
    btnRegisterTab.classList.remove('active');
    registerFields.style.display = 'none';
    authSubmit.textContent = 'Login';
  } else {
    btnRegisterTab.classList.add('active');
    btnLoginTab.classList.remove('active');
    registerFields.style.display = 'block';
    authSubmit.textContent = 'Register';
  }
}

// ── Auth ─────────────────────────────────────────────────────
async function handleAuth(e) {
  e.preventDefault();
  authError.textContent = '';
  if (authSubmit.disabled) return;
  authSubmit.disabled = true;
  const orig = authSubmit.textContent;
  authSubmit.textContent = 'Processing…';

  const username = document.getElementById('auth-username').value.trim();
  const password = document.getElementById('auth-password').value;

  try {
    if (authSubmit.textContent === 'Processing…' && orig === 'Register') {
      // handled below via orig check — use mode tracking instead
    }
    const mode = orig === 'Login' ? 'login' : 'register';

    if (mode === 'register') {
      const fullName    = document.getElementById('reg-fullName').value.trim();
      const phoneNumber = document.getElementById('reg-phone').value.trim();
      const res = await fetch(`${API_BASE}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password, fullName, phoneNumber })
      });
      if (!res.ok) throw new Error(await getErrorMsg(res));
      await loginRequest(username, password);
    } else {
      await loginRequest(username, password);
    }
  } catch (err) {
    authError.textContent = err.message;
  } finally {
    authSubmit.disabled = false;
    authSubmit.textContent = orig;
  }
}

async function loginRequest(username, password) {
  sessionStorage.clear();
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  if (!res.ok) throw new Error(await getErrorMsg(res));
  showDashboard();
}

async function handleLogout() {
  if (btnLogout.disabled) return;
  btnLogout.disabled = true;
  await fetch(`${API_BASE}/auth/logout`, { method: 'POST' }).catch(() => {});
  sessionStorage.clear();
  btnLogout.disabled = false;
  closeModal();
  showAuth();
}

// ── Session Check ─────────────────────────────────────────────
async function checkSession() {
  try {
    const res = await fetch(`${API_BASE}/categories`);
    if (res.ok) {
      const body = await res.json();
      categories = body.data;
      setCachedCategories(categories);
      showDashboard();
    } else {
      showAuth();
    }
  } catch {
    showAuth();
  }
}

// ── Views ─────────────────────────────────────────────────────
function showAuth() {
  dashboardView.style.display = 'none';
  authView.style.display      = 'block';
  authView.style.animation    = 'none';
  authView.offsetHeight;
  authView.style.animation    = 'slideUp 0.45s cubic-bezier(0.16, 1, 0.3, 1)';
}

async function showDashboard() {
  authView.style.display       = 'none';
  dashboardView.style.display  = 'block';
  dashboardView.style.animation = 'none';
  dashboardView.offsetHeight;
  dashboardView.style.animation = 'slideUp 0.45s cubic-bezier(0.16, 1, 0.3, 1)';
  await refreshDashboard();
}

// ── Dashboard Refresh ─────────────────────────────────────────
async function refreshDashboard() {
  const now   = new Date();
  const year  = now.getFullYear();
  const month = now.getMonth() + 1;

  const cachedCats = getCachedCategories();
  const fetchList  = [
    fetch(`${API_BASE}/transactions`),
    fetch(`${API_BASE}/reports/monthly/${year}/${month}`)
  ];
  if (!cachedCats) fetchList.push(fetch(`${API_BASE}/categories`));

  try {
    const responses = await Promise.all(fetchList);

    // Handle 401 → go back to login
    if (responses.some(r => r.status === 401)) {
      sessionStorage.clear();
      showAuth();
      return;
    }

    const [txsRes, reportRes, catsRes] = responses;

    const [txsBody, reportBody, catsBody] = await Promise.all([
      txsRes.json(),
      reportRes.json(),
      catsRes ? catsRes.json() : Promise.resolve(null)
    ]);

    // Update categories
    if (catsBody) {
      categories = catsBody.data;
      setCachedCategories(categories);
    } else {
      categories = cachedCats || [];
    }

    populateCategoryDropdown(txCategorySelect, null); // all categories

    // Transactions
    renderTransactions(txsBody.data || []);

    // Report
    const report = reportBody.data;
    const income  = Number(report.totalIncome  || 0);
    const expense = Number(report.totalExpense || 0);
    const balance = income - expense;

    elMonthlyIncome .textContent = `+₹${fmt(income)}`;
    elMonthlyExpense.textContent = `-₹${fmt(expense)}`;
    elTotalBalance  .textContent = `₹${fmt(balance)}`;

    // Color balance dynamically
    elTotalBalance.className = balance >= 0 ? 'balance-color' : 'negative';

  } catch (err) {
    console.error('Dashboard refresh failed:', err);
  }
}

function renderTransactions(txs) {
  transactionList.innerHTML = '';
  if (!txs.length) {
    transactionList.innerHTML = '<li style="color:var(--text-muted);text-align:center;padding:20px;">No transactions yet.</li>';
    return;
  }
  txs.forEach(tx => {
    const cat = categories.find(c => c.name === tx.categoryName);
    const isIncome = cat?.type === 'INCOME';
    const li = document.createElement('li');
    li.className = 'tx-item';
    li.innerHTML = `
      <div class="tx-info">
        <span class="tx-cat">${tx.categoryName}</span>
        <span class="tx-date">${tx.date}${tx.description ? ' · ' + tx.description : ''}</span>
      </div>
      <div class="tx-amt ${isIncome ? 'positive' : 'negative'}">${isIncome ? '+' : '-'}₹${fmt(tx.amount)}</div>
    `;
    transactionList.appendChild(li);
  });
}

function populateCategoryDropdown(selectEl, filterType) {
  const prev = selectEl.value;
  selectEl.innerHTML = '<option value="" disabled selected>Category</option>';
  const filtered = filterType ? categories.filter(c => c.type === filterType) : categories;
  filtered.forEach(cat => {
    const opt = document.createElement('option');
    opt.value       = cat.name;
    opt.textContent = `${cat.name} (${cat.type})`;
    selectEl.appendChild(opt);
  });
  if (prev) selectEl.value = prev;
}

// ── Add Transaction (main form) ────────────────────────────────
async function handleAddTransaction(e) {
  e.preventDefault();
  const btn = txForm.querySelector('button[type="submit"]');
  if (btn.disabled) return;
  btn.disabled = true;
  const orig = btn.textContent;
  btn.textContent = 'Adding…';

  const amount      = parseFloat(document.getElementById('tx-amount').value);
  const date        = document.getElementById('tx-date').value;
  const categoryName= document.getElementById('tx-category').value;
  const description = document.getElementById('tx-desc').value.trim();
  const type        = categories.find(c => c.name === categoryName)?.type || 'EXPENSE';

  try {
    const res = await fetch(`${API_BASE}/transactions`, {
      method : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body   : JSON.stringify({ amount, date, categoryName, description, type })
    });
    if (res.ok) {
      txForm.reset();
      setDefaultDate();
      clearCachedCategories();
      await refreshDashboard();
    } else {
      alert(await getErrorMsg(res));
    }
  } catch {
    alert('Network error. Please try again.');
  } finally {
    btn.disabled    = false;
    btn.textContent = orig;
  }
}

// ── Modal Logic ────────────────────────────────────────────────
function openModal(mode) {
  modalMode       = mode;
  selectedCatName = null;
  modalAmount.value = '';
  modalDesc.value   = '';
  modalError.textContent = '';
  document.getElementById('modal-date').value = new Date().toISOString().split('T')[0];

  if (mode === 'INCOME') {
    modalTitle.textContent    = '💰 Add Income';
    modalSubtitle.textContent = 'Select a source and enter the amount received';
    modalSubmitBtn.textContent = 'Add Income';
    modalSubmitBtn.style.background = 'linear-gradient(135deg, #00c896, #007a5c)';
  } else {
    modalTitle.textContent    = '💸 Add Expense';
    modalSubtitle.textContent = 'Select a category and enter the amount spent';
    modalSubmitBtn.textContent = 'Add Expense';
    modalSubmitBtn.style.background = 'linear-gradient(135deg, #ff4f6a, #b91c3b)';
  }

  // Build category chips filtered by type
  buildCategoryChips(mode);

  modalOverlay.classList.remove('hidden');
  setTimeout(() => modalAmount.focus(), 100);
}

function closeModal() {
  modalOverlay.classList.add('hidden');
  modalError.textContent = '';
}

function buildCategoryChips(filterType) {
  modalCategoryGrid.innerHTML = '';
  const filtered = categories.filter(c => c.type === filterType);

  if (!filtered.length) {
    modalCategoryGrid.innerHTML = '<p style="color:var(--text-muted);font-size:13px;grid-column:1/-1;">No categories found.</p>';
    return;
  }

  filtered.forEach(cat => {
    const chip = document.createElement('button');
    chip.type      = 'button';
    chip.className = 'cat-chip';
    chip.textContent = cat.name;
    chip.dataset.name = cat.name;
    chip.addEventListener('click', () => {
      document.querySelectorAll('.cat-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      selectedCatName = cat.name;
      modalError.textContent = '';
    });
    modalCategoryGrid.appendChild(chip);
  });
}

async function handleModalSubmit() {
  modalError.textContent = '';

  // Validate
  if (!selectedCatName) {
    modalError.textContent = '⚠ Please select a category.';
    return;
  }
  const amount = parseFloat(modalAmount.value);
  if (!amount || amount <= 0) {
    modalError.textContent = '⚠ Please enter a valid amount.';
    modalAmount.focus();
    return;
  }
  const date = document.getElementById('modal-date').value;
  if (!date) {
    modalError.textContent = '⚠ Please select a date.';
    return;
  }

  modalSubmitBtn.disabled    = true;
  modalSubmitBtn.textContent = 'Adding…';

  const description = modalDesc.value.trim();
  const type        = modalMode;

  try {
    const res = await fetch(`${API_BASE}/transactions`, {
      method : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body   : JSON.stringify({ amount, date, categoryName: selectedCatName, description, type })
    });

    if (res.ok) {
      closeModal();
      clearCachedCategories();
      // Visual feedback on the card
      flashCard(type);
      await refreshDashboard();
    } else {
      modalError.textContent = '✕ ' + (await getErrorMsg(res));
    }
  } catch {
    modalError.textContent = '✕ Network error. Please try again.';
  } finally {
    modalSubmitBtn.disabled    = false;
    modalSubmitBtn.textContent = type === 'INCOME' ? 'Add Income' : 'Add Expense';
  }
}

function flashCard(type) {
  const card = type === 'INCOME' ? cardIncome : cardExpense;
  card.style.transition = 'box-shadow 0.15s, transform 0.15s';
  const glowColor = type === 'INCOME' ? 'rgba(0,200,150,0.6)' : 'rgba(255,79,106,0.6)';
  card.style.boxShadow = `0 0 30px ${glowColor}`;
  card.style.transform  = 'scale(1.03)';
  setTimeout(() => {
    card.style.boxShadow = '';
    card.style.transform  = '';
  }, 600);
}
