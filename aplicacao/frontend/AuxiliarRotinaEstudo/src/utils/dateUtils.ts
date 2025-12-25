export const validarFormatoData = (dataStr: string): boolean => {
    const exprRegular = /^\d{2}\/\d{2}\/\d{4}$/;
     
    if(!exprRegular.test(dataStr)) return false;
    
    const [dia, mes, ano] = dataStr.split('/').map(Number) as [number, number, number];
    
    if(mes < 1 || mes > 12) return false;
    if(dia < 1 || dia > 31) return false;
    
    const data = new Date(ano, mes - 1, dia);

    return (
        data.getDate() === dia &&
        data.getMonth() === mes - 1 &&
        data.getFullYear() === ano
    );
};

export const formatarDataParaPTBR = (data: Date): string => {
    const dia = String(data.getDate()).padStart(2, '0');
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const ano = String(data.getFullYear());
    return `${dia}/${mes}/${ano}`;
};

export const getDataHoje = (): string => {
    return formatarDataParaPTBR(new Date());
};

export const converterStringParaData = (dataString: string): Date => {
    const [dia, mes, ano] = dataString.split('/').map(Number) as [number, number, number];
    return new Date(ano, mes - 1, dia);
};

export const formatarDataParaISO = (dataString: string): string | undefined => {
    if (!dataString) return '';
    const data = converterStringParaData(dataString);
    return data.toISOString().split('T')[0];
};

export const verificarDataFutura = (dataString: string): boolean => {
    const data = converterStringParaData(dataString);
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0); 
    return data > hoje;
};

export const verificarDataPassada = (dataString: string): boolean => {
  const data = converterStringParaData(dataString);
  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);
  return data < hoje;
};

export const verificarDataHoje = (dataString: string): boolean => {
  const data = converterStringParaData(dataString);
  const hoje = new Date();
  return formatarDataParaPTBR(data) === formatarDataParaPTBR(hoje);
};

export const formatarDataParaExibicao = (
    dataInput: Date | string | null,
    locale: string = 'pt-BR',
    options: Intl.DateTimeFormatOptions = { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' }
): string => {
    if (!dataInput) return '';

    let data: Date;
    if (typeof dataInput === 'string') {
        if (validarFormatoData(dataInput)) {
            data = converterStringParaData(dataInput);
        } else {
            const parsed = new Date(dataInput);
            if (isNaN(parsed.getTime())) return dataInput;
            data = parsed;
        }
    } else {
        data = dataInput;
    }

    const formatted = data.toLocaleDateString(locale, options);
    return formatted.replace(/^^\p{L}/u, c => c.toUpperCase());
};

