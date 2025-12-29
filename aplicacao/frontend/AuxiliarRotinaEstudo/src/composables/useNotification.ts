import { ref } from 'vue';

type Color = "success" | "error" | "warning" | "info"; 

const notificationBar = ref({
    show: false,
    message: "",
    color: "info" as Color,
});

function showNotification(message: string, color: Color = "info") {
    notificationBar.value = { show: true, message, color };
}

function showSuccess(message: string) {
    showNotification(message, 'success');
}

function showError(message: string) {
    showNotification(message, 'error');
}

function showWarning(message: string) {
    showNotification(message, 'warning');
}

function showInfo(message: string) {
    showNotification(message, 'info');
}

export function useNotification() {
    return { 
        notificationBar, 
        showNotification,
        showSuccess,
        showError,
        showWarning,
        showInfo
    };
}