let currentMode = 'login'; // 'login' or 'register'
const API_BASE = 'https://personal-finance-manager-7jbx.onrender.com/api';

// UI Elements
const authView = document.getElementById('auth-view');
const dashboardView = document.getElementById('dashboard-view');
const btnLoginTab = document.getElementById('tab-login');
const btnRegisterTab = document.getElementById('tab-register');
const registerFields = document.getElementById('register-fields');
const authForm = document.getElementById('auth-form');
const authError = document.getElementById('auth-error');
const authSubmit = document.getElementById('auth-submit');
const btnLogout = document.getElementById('btn-logout');
const txForm = document.getElementById('tx-form');
const txCategorySelect = document.getElementById('tx-category');
const transactionList = document.getElementById('transaction-list');
const elTotalBalance = document.getElementById('total-balance');
const elMonthlyIncome = document.getElementById('monthly-income');
const elMonthlyExpense = document.getElementById('monthly-expense');

let categories = [];

// Init
checkSession();

// Event Listeners
btnLoginTab.addEventListener('click', () => switchAuthMode('login'));
btnRegisterTab.addEventListener('click', () => switchAuthMode('register'));
authForm.addEventListener('submit', handleAuth);
btnLogout.addEventListener('click', handleLogout);
txForm.addEventListener('submit', handleAddTransaction);

function switchAuthMode(mode) {
  currentMode = mode;
  authError.textContent = '';
  if(mode === 'login') {
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

async function handleAuth(e) {
  e.preventDefault();
  authError.textContent = '';
  const username = document.getElementById('auth-username').value;
  const password = document.getElementById('auth-password').value;

  try {
    if (currentMode === 'register') {
      const fullName = document.getElementById('reg-fullName').value;
      const phoneNumber = document.getElementById('reg-phone').value;
      const res = await fetch(`${API_BASE}/auth/register`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password, fullName, phoneNumber})
      });
      if(!res.ok) throw new Error(await getErrorMsg(res));
      
      // Auto login after register
      await login(username, password);
    } else {
      await login(username, password);
    }
  } catch (err) {
    authError.textContent = err.message;
  }
}

async function login(username, password) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({username, password})
  });
  if(!res.ok) throw new Error(await getErrorMsg(res));
  
  showDashboard();
}

async function handleLogout() {
  await fetch(`${API_BASE}/auth/logout`, { method: 'POST' });
  showAuth();
}

async function checkSession() {
  try {
    const res = await fetch(`${API_BASE}/categories`);
    if(res.ok) {
      showDashboard();
    } else {
      showAuth();
    }
  } catch (e) {
    showAuth();
  }
}

function showAuth() {
  dashboardView.style.display = 'none';
  authView.style.display = 'block';
  authView.style.animation = 'none';
  authView.offsetHeight; // trigger reflow
  authView.style.animation = 'slideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1)';
}

async function showDashboard() {
  authView.style.display = 'none';
  dashboardView.style.display = 'block';
  dashboardView.style.animation = 'none';
  dashboardView.offsetHeight; // trigger reflow
  dashboardView.style.animation = 'slideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1)';
  
  await loadCategories();
  await loadDashboardData();
}

async function loadCategories() {
  const res = await fetch(`${API_BASE}/categories`);
  categories = await res.json();
  
  txCategorySelect.innerHTML = '<option value="" disabled selected>Category</option>';
  categories.forEach(cat => {
    const opt = document.createElement('option');
    opt.value = cat.name;
    opt.textContent = `${cat.name} (${cat.type})`;
    txCategorySelect.appendChild(opt);
  });
}

async function loadDashboardData() {
  // Load Transactions
  const res = await fetch(`${API_BASE}/transactions`);
  const txs = await res.json();
  
  transactionList.innerHTML = '';
  if(txs.length === 0) {
    transactionList.innerHTML = '<li style="color:var(--text-muted); text-align:center;">No transactions found.</li>';
  } else {
    txs.forEach(tx => {
      const li = document.createElement('li');
      li.className = 'tx-item';
      
      const isIncome = categories.find(c => c.name === tx.category)?.type === 'INCOME';
      const colorClass = isIncome ? 'positive' : 'negative';
      const sign = isIncome ? '+' : '-';
      
      li.innerHTML = `
        <div class="tx-info">
          <span class="tx-cat">${tx.category}</span>
          <span class="tx-date">${tx.date} ${tx.description ? ' - '+tx.description : ''}</span>
        </div>
        <div class="tx-amt ${colorClass}">${sign}$${tx.amount.toFixed(2)}</div>
      `;
      transactionList.appendChild(li);
    });
  }

  // Load Reports for current month to compute stats
  const date = new Date();
  const year = date.getFullYear();
  const month = date.getMonth() + 1; // 1-12
  
  const repRes = await fetch(`${API_BASE}/reports/monthly/${year}/${month}`);
  if(repRes.ok) {
    const report = await repRes.json();
    elMonthlyIncome.textContent = `+$${report.totalIncome.toFixed(2)}`;
    elMonthlyExpense.textContent = `-$${report.totalExpenses.toFixed(2)}`;
    elTotalBalance.textContent = `$${(report.totalIncome - report.totalExpenses).toFixed(2)}`;
  }
}

async function handleAddTransaction(e) {
  e.preventDefault();
  const amount = parseFloat(document.getElementById('tx-amount').value);
  const date = document.getElementById('tx-date').value;
  const category = document.getElementById('tx-category').value;
  const description = document.getElementById('tx-desc').value;

  const res = await fetch(`${API_BASE}/transactions`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({ amount, date, category, description })
  });

  if(res.ok) {
    txForm.reset();
    await loadDashboardData();
  } else {
    alert(await getErrorMsg(res));
  }
}

async function getErrorMsg(res) {
  try {
    const data = await res.json();
    return data.message || 'An error occurred';
  } catch (e) {
    return `HTTP Error ${res.status}`;
  }
}
