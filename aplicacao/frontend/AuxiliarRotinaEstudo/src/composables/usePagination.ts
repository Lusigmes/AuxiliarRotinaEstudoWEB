import { ref } from "vue";

export function usePagination<T>(
    fetchFunction: (page: number, size: number) => Promise<{content: T[]; totalPages: number; totalElements: number}>,
    initialSize = 10
) {
    const items = ref<T[]>([]);
    const page = ref(0);
    const size = ref(initialSize);
    const totalPages = ref(0);
    const totalElements = ref(0);
    const loading = ref(false);

    const atualizarPagina = async (pagina = page.value) => {
        loading.value = true;
        try {
            const response = await fetchFunction(pagina, size.value);
            items.value = response.content;
            totalPages.value = response.totalPages;
            totalElements.value = response.totalElements;
            page.value = pagina;
        } finally {
            loading.value = false;
        }
    };

    return {
        items,
        page,
        size,
        totalPages,
        totalElements,
        loading,
        atualizarPagina
    };
}