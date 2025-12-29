import { ref } from "vue";
import { 
    logarUsuario, 
    registrarUsuario, 
    getUsuarioAutenticado,
    refreshToken as apiRefreshToken 
} from "@/api/UsuarioAuthService";
import type {
    UsuarioResponseInterface, 
    RegistroUsuarioInterface, 
    LoginUsuarioInterface, 
    RefreshTokenRequest
} from "@/types";
import axios from "axios";
import { useNotification } from '@/composables/useNotification';
import { useRouter } from 'vue-router';

const TOKEN_TOTAL_TIME = 10 * 60 * 1000;
const SHOW_MODAL_THRESHOLD = 1 * 60 * 1000;

const token = ref<string | null>(localStorage.getItem("jwt"));
const refreshTokenValue = ref<string | null>(localStorage.getItem("refreshToken"));
const usuario = ref<UsuarioResponseInterface | null>(null);
const error = ref<string | null>(null);
const loading = ref(false);

const showRenewalModal = ref(false);
const tokenExpirationTime = ref<number | null>(null);
let expirationCheckInterval: ReturnType<typeof setInterval> | null = null;

export function useAuth() {
    const { showNotification } = useNotification();
    const router = useRouter();

    const setupAxiosInterceptors = () => {
        axios.interceptors.request.use(
            (config) => {
                if (token.value) {
                    config.headers.Authorization = `Bearer ${token.value}`;
                }
                return config;
            },
            (error) => Promise.reject(error)
        );

        axios.interceptors.response.use(
            (response) => response,
            async (error) => {
                const originalRequest = error.config;
                
                if (error.response?.status === 401 && !originalRequest._retry) {
                    originalRequest._retry = true;
                    
                    if (showRenewalModal.value) {
                        return Promise.reject(error);
                    }
                    
                    try {
                        const newToken = await refreshAuthToken();
                        if (newToken) {
                            originalRequest.headers.Authorization = `Bearer ${newToken}`;
                            return axios(originalRequest);
                        }
                    } catch (refreshError) {
                        console.error("Token expirado e não foi possível renovar:", refreshError);
                        handleSessionExpired();
                    }
                }
                
                return Promise.reject(error);
            }
        );
    };
    
const checkTokenExpiration = () => {
    if (!token.value) {
        tokenExpirationTime.value = null;
        return;
    }

    try {
        const payloadBase64 = token.value.split('.')[1];
        const payloadJson = atob(payloadBase64!);
        const payload = JSON.parse(payloadJson);
        
        const expiration = payload.exp * 1000; 
        const now = Date.now();
        const timeUntilExpiration = expiration - now;
        
        tokenExpirationTime.value = timeUntilExpiration;
        
        if (timeUntilExpiration < SHOW_MODAL_THRESHOLD && !showRenewalModal.value && token.value) {
            showRenewalModal.value = true;
        }
        
        if (timeUntilExpiration <= 0 && token.value) {
            handleSessionExpired();
        }
        
    } catch (error) {
        console.error("Erro ao verificar expiração do token:", error);
    }
};

const startTokenExpirationMonitor = () => {
    if (expirationCheckInterval) {
        clearInterval(expirationCheckInterval);
    }
    
    expirationCheckInterval = setInterval(() => {
        if (token.value) {
            checkTokenExpiration();
        }
    }, 1000); 
};

    const stopTokenExpirationMonitor = () => {
        if (expirationCheckInterval) {
            clearInterval(expirationCheckInterval);
            expirationCheckInterval = null;
        }
    };

    const refreshAuthToken = async (): Promise<string | null> => {
        if (!refreshTokenValue.value) {
            console.error("Nenhum refresh token disponível");
            return null;
        }

        try {
            console.log("Renovando token...");
            
            const request: RefreshTokenRequest = {
                refreshToken: refreshTokenValue.value!
            };
            
            const response = await apiRefreshToken(request);
            
            token.value = response.token;
            localStorage.setItem("jwt", response.token);
            
            if (response.refreshToken) {
                refreshTokenValue.value = response.refreshToken;
                localStorage.setItem("refreshToken", response.refreshToken);
            }
            
            setupAxiosHeaders();
            showNotification("Sessão renovada com sucesso!", "success");
            
            showRenewalModal.value = false;
            setTimeout(() => checkTokenExpiration(), 100);
            
            return response.token;
        } catch (error) {
            console.error("Erro ao renovar token:", error);
            showNotification("Não foi possível renovar a sessão", "error");
            throw error;
        }
    };

    const handleSessionExpired = () => {
        if (showRenewalModal.value) return; 
        
        showNotification("Sua sessão expirou", "error");
        logout();
        setTimeout(() => {
            router.push('/');
        }, 1000);
    };

    const setupAxiosHeaders = () => {
        if (token.value) {
            axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`;
        } else {
            delete axios.defaults.headers.common['Authorization'];
        }
    };

    const login = async (dadosUsuario: LoginUsuarioInterface) => {
        try {
            loading.value = true;
            error.value = null;
            
            const response = await logarUsuario(dadosUsuario);
            
            token.value = response.token;
            refreshTokenValue.value = response.refreshToken || null;
            
            localStorage.setItem("jwt", response.token);
            if (response.refreshToken) {
                localStorage.setItem("refreshToken", response.refreshToken);
            }
            
            setupAxiosHeaders();
            setupAxiosInterceptors();
            await fetchUsuario();
            
            startTokenExpirationMonitor();
            
            showNotification("Login realizado com sucesso!", "success");
            return true;
        } catch (err: any) {
            error.value = err.response?.data?.message || "Falha no login";
            showNotification(error.value!, "error");
            return false;
        } finally {
            loading.value = false;
        }
    };

    const logout = () => {
        token.value = null;
        refreshTokenValue.value = null;
        usuario.value = null;
        showRenewalModal.value = false;
        localStorage.removeItem("jwt");
        localStorage.removeItem("refreshToken");
        delete axios.defaults.headers.common['Authorization'];
        
        stopTokenExpirationMonitor();
        
        showNotification("Logout realizado com sucesso!", "info");
    };
  
    const fetchUsuario = async () => {
        try {
            if (token.value) {
                usuario.value = await getUsuarioAutenticado();
            }
        } catch (err) {
            console.error("Erro ao buscar usuário:", err);
            usuario.value = null;
        }
    };

    const initializeAuth = () => {
        if (token.value) {
            setupAxiosHeaders();
            setupAxiosInterceptors();
            fetchUsuario();
            startTokenExpirationMonitor();
        }
    };

    initializeAuth();

    const registro = async (dadosUsuario: RegistroUsuarioInterface) => {
        try {
            loading.value = true;
            error.value = null;
            
            await registrarUsuario(dadosUsuario);
            const success = await login({email: dadosUsuario.email, senha: dadosUsuario.senha});
            
            if (success) {
                showNotification("Conta criada com sucesso!", "success");
            }
            
            return success;
        } catch (err: any) {
            error.value = err.response?.data?.message || "Falha no registro";
            showNotification(error.value!, "error");
            return false;
        } finally {
            loading.value = false;
        }
    };

    const handleTokenRenewal = async (): Promise<boolean> => {
        try {
            await refreshAuthToken();
            return true;
        } catch {
            return false;
        }
    };

    const handleTokenRenewalCancel = () => {
        logout();
        router.push('/');
    };

    return { 
        token, 
        usuario, 
        error,
        loading,
        showRenewalModal,
        tokenExpirationTime,
        login, 
        registro, 
        logout, 
        fetchUsuario,
        refreshAuthToken,
        handleTokenRenewal,
        handleTokenRenewalCancel
    };
}