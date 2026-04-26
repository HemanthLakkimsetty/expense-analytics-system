

const API_BASE = '/FinanceTracker/api';

async function apiCall(endpoint, method = 'GET', params = null) {
    let url = API_BASE + endpoint;

    const options = { method, headers: {} };

    if (params) {
        const query = new URLSearchParams(params);
        if (method === 'GET' || method === 'DELETE') {
            url += '?' + query.toString();
        } else {

            options.headers['Content-Type'] = 'application/x-www-form-urlencoded';
            options.body = query.toString();
        }
    }

    const response = await fetch(url, options);
    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.error || `HTTP ${response.status}`);
    }
    return data;
}

function ensureToastContainer() {
    if (!document.getElementById('toast-container')) {
        const el = document.createElement('div');
        el.id = 'toast-container';
        document.body.appendChild(el);
    }
}

function showToast(message, type = 'success', duration = 3000) {
    ensureToastContainer();
    const container = document.getElementById('toast-container');

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(16px)';
        toast.style.transition = 'all 0.3s';
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        minimumFractionDigits: 2
    }).format(amount || 0);
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    return new Date(dateStr + 'T00:00:00').toLocaleDateString('en-IN', {
        year: 'numeric', month: 'short', day: 'numeric'
    });
}

function setActiveNavLink() {
    const currentPage = window.location.pathname.split('/').pop();
    document.querySelectorAll('.nav-link').forEach(link => {
        const href = link.getAttribute('href');
        if (href && href === currentPage) {
            link.classList.add('active');
        }
    });
}

function openModal(id) {
    document.getElementById(id).classList.add('open');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('open');
}

document.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('open');
    }
});

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal-overlay.open').forEach(m => m.classList.remove('open'));
    }
});

document.addEventListener('DOMContentLoaded', setActiveNavLink);
