export interface RegistroUsuarioInterface{
    nome: string;
    email: string;
    senha: string;
    // tipoUsuario: "CLIENTE" | "PRESTADOR";
    // roleUsuario:string;

}
export interface UsuarioResponseInterface{
    id:number;
    nome: string;
    email: string;
    // roleUsuario:string;
}

export interface LoginUsuarioInterface{
    email: string;
    senha: string;
}
export interface LoginTokenResponseInterface{
    token:string;
    expiresIn: number
}