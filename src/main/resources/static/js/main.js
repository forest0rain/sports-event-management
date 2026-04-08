/**
 * 体育赛事管理平台 - 主脚本
 */

// 等待DOM加载完成
document.addEventListener('DOMContentLoaded', function() {
    
    // 初始化工具提示
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // 初始化弹出框
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function(popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
    
    // 自动关闭alert
    setTimeout(function() {
        var alerts = document.querySelectorAll('.alert:not(.alert-permanent)');
        alerts.forEach(function(alert) {
            var bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);
    
});

/**
 * 格式化日期
 */
function formatDate(date, format) {
    if (!date) return '';
    
    format = format || 'yyyy-MM-dd';
    
    var d = new Date(date);
    var year = d.getFullYear();
    var month = String(d.getMonth() + 1).padStart(2, '0');
    var day = String(d.getDate()).padStart(2, '0');
    var hours = String(d.getHours()).padStart(2, '0');
    var minutes = String(d.getMinutes()).padStart(2, '0');
    var seconds = String(d.getSeconds()).padStart(2, '0');
    
    return format
        .replace('yyyy', year)
        .replace('MM', month)
        .replace('dd', day)
        .replace('HH', hours)
        .replace('mm', minutes)
        .replace('ss', seconds);
}

/**
 * 确认对话框
 */
function confirmAction(message, callback) {
    if (confirm(message)) {
        callback();
    }
}

/**
 * 显示加载状态
 */
function showLoading(button) {
    var originalText = button.innerHTML;
    button.disabled = true;
    button.innerHTML = '<span class="loading me-2"></span>处理中...';
    button.setAttribute('data-original-text', originalText);
}

/**
 * 隐藏加载状态
 */
function hideLoading(button) {
    var originalText = button.getAttribute('data-original-text');
    button.disabled = false;
    button.innerHTML = originalText;
}

/**
 * AJAX提交表单
 */
function submitForm(form, successCallback, errorCallback) {
    var formData = new FormData(form);
    var button = form.querySelector('button[type="submit"]');
    
    if (button) showLoading(button);
    
    fetch(form.action, {
        method: form.method || 'POST',
        body: formData
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        }
        throw new Error('Network response was not ok');
    })
    .then(data => {
        if (button) hideLoading(button);
        if (successCallback) successCallback(data);
    })
    .catch(error => {
        if (button) hideLoading(button);
        if (errorCallback) errorCallback(error);
        console.error('Error:', error);
    });
}

/**
 * 表单验证
 */
function validateForm(form) {
    var inputs = form.querySelectorAll('input[required], select[required], textarea[required]');
    var isValid = true;
    
    inputs.forEach(function(input) {
        if (!input.value.trim()) {
            input.classList.add('is-invalid');
            isValid = false;
        } else {
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
        }
    });
    
    return isValid;
}

/**
 * 导出数据为CSV
 */
function exportToCSV(tableId, filename) {
    var table = document.getElementById(tableId);
    if (!table) return;
    
    var rows = table.querySelectorAll('tr');
    var csv = [];
    
    rows.forEach(function(row) {
        var cols = row.querySelectorAll('td, th');
        var rowData = [];
        cols.forEach(function(col) {
            rowData.push('"' + col.innerText.replace(/"/g, '""') + '"');
        });
        csv.push(rowData.join(','));
    });
    
    var csvContent = csv.join('\n');
    var blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' });
    var link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = filename || 'export.csv';
    link.click();
}

/**
 * 打印页面
 */
function printPage() {
    window.print();
}

/**
 * 复制到剪贴板
 */
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(function() {
        alert('已复制到剪贴板');
    }).catch(function(err) {
        console.error('复制失败:', err);
    });
}

/**
 * 数字格式化
 */
function formatNumber(num, decimals) {
    decimals = decimals || 0;
    return Number(num).toFixed(decimals);
}

/**
 * 成绩格式化
 */
function formatScore(score, unit) {
    if (!score) return '-';
    
    // 计时项目 (秒)
    if (unit === '秒') {
        var minutes = Math.floor(score / 60);
        var seconds = (score % 60).toFixed(2);
        return minutes > 0 ? minutes + ':' + seconds.padStart(5, '0') + '秒' : seconds + '秒';
    }
    
    // 距离项目 (米)
    if (unit === '米') {
        return Number(score).toFixed(2) + '米';
    }
    
    return score + (unit || '');
}

/**
 * 排名颜色
 */
function getRankColor(rank) {
    switch (rank) {
        case 1: return 'gold';
        case 2: return 'silver';
        case 3: return 'bronze';
        default: return 'default';
    }
}

/**
 * 状态文本
 */
function getStatusText(status) {
    var statusMap = {
        'DRAFT': '草稿',
        'REGISTRATION': '报名中',
        'ONGOING': '进行中',
        'FINISHED': '已结束',
        'CANCELLED': '已取消',
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已拒绝',
        'SCHEDULED': '已安排',
        'COMPLETED': '已完成',
        'VALID': '有效',
        'INVALID': '无效',
        'DNS': '未起跑',
        'DNF': '未完赛',
        'DQ': '取消资格'
    };
    return statusMap[status] || status;
}

/**
 * 状态徽章类
 */
function getStatusBadgeClass(status) {
    var classMap = {
        'DRAFT': 'bg-secondary',
        'REGISTRATION': 'bg-primary',
        'ONGOING': 'bg-success',
        'FINISHED': 'bg-info',
        'CANCELLED': 'bg-danger',
        'PENDING': 'bg-warning',
        'APPROVED': 'bg-success',
        'REJECTED': 'bg-danger'
    };
    return classMap[status] || 'bg-secondary';
}
