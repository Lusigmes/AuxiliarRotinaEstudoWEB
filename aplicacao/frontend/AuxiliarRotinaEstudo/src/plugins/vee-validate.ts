import { configure, defineRule } from 'vee-validate'
import { localize, setLocale } from '@vee-validate/i18n'
import pt_BR from '@vee-validate/i18n/dist/locale/pt_BR.json'
import * as rules from '@vee-validate/rules'

// Define todas as regras
Object.keys(rules).forEach(rule => {
  defineRule(rule, rules[rule])
})

configure({
  generateMessage: localize({
    pt_BR: {
      ...pt_BR,
      names: {
        nome: 'Nome completo',
        email: 'E-mail',
        senha: 'Senha',
        confirmarSenha: 'Confirmar senha'
      }
    }
  }),
  validateOnBlur: true,
  validateOnChange: true,
  validateOnInput: false,
  validateOnModelUpdate: true,
})

setLocale('pt_BR')