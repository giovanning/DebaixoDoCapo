# 📱 Debaixo do Capô

> Identifica as tecnologias utilizadas nos aplicativos instalados no seu dispositivo Android.

---

## Sobre o projeto

**Debaixo do Capô** é um aplicativo Android nativo, de código aberto e sem fins lucrativos, que analisa os pacotes dos aplicativos instalados no dispositivo e tenta identificar o framework ou tecnologia utilizada em cada um deles.

A motivação do projeto é educacional: entender como diferentes apps são construídos, quais stacks predominam no ecossistema Android e como é possível fazer essa inferência a partir dos artefatos gerados no processo de build.

> ⚠️ A detecção é baseada em heurísticas e **não garante 100% de precisão**. A presença de certas pastas ou arquivos indica *fortemente* o uso de uma tecnologia, mas não é definitiva — por exemplo, um projeto full Compose ainda pode conter a pasta `res/layout/` para drawables ou outros recursos.

---

## Como funciona

O app inspeciona o APK de cada aplicativo instalado procurando por arquivos, bibliotecas nativas e estruturas de diretórios que são característicos de cada stack. A lógica de detecção funciona da seguinte forma:

| Stack | Indicadores verificados |
|---|---|
| 🐦 **Flutter** | `lib/libflutter.so`, `assets/flutter_assets/` |
| ⚛️ **React Native** | `lib/libreactnativejni.so`, `assets/index.android.bundle` |
| 🖼️ **XML (Views)** | `res/layout/` *(com ressalvas — veja abaixo)* |
| 🎨 **Jetpack Compose** | Referências a `androidx.compose` nas classes |
| 🌐 **Kotlin Multiplatform** | `shared.kotlin_module`, diretórios `kmp` ou `kotlin-multiplatform` |
| 🤖 **Stack nativa oculta** | Nenhum dos indicadores acima encontrado |

### ⚠️ Nota sobre `res/layout/`

A presença da pasta `res/layout/` **não significa necessariamente** que o app usa XML para suas telas. Projetos construídos inteiramente com Jetpack Compose ainda podem conter essa pasta para hospedar drawables, layouts de widgets, notificações ou recursos de bibliotecas de terceiros. O app tenta cruzar esse dado com outros indicadores para dar um resultado mais preciso.

---

## Tecnologias utilizadas

- **Linguagem:** Kotlin
- **UI:** Jetpack Compose
- **Mínimo SDK:** Android 8.0 (API 26)
- **Target SDK:** Android 16 (API 36)
- Sem dependências externas além das bibliotecas AndroidX padrão

---

## Como executar

### Pré-requisitos

- Android Studio Hedgehog ou superior
- JDK 17+
- Dispositivo ou emulador com Android 8.0+

### Passos

```bash
# Clone o repositório
git clone https://github.com/giovanning/DebaixoDoCapo.git

# Abra no Android Studio
# File > Open > selecione a pasta do projeto

# Execute no dispositivo ou emulador
# Run > Run 'app'
```

Não são necessárias configurações adicionais, chaves de API ou permissões especiais além de `QUERY_ALL_PACKAGES`, que o app solicita para conseguir listar todos os aplicativos instalados.

---

## Permissões

O app solicita apenas a permissão abaixo:

| Permissão | Motivo |
|---|---|
| `QUERY_ALL_PACKAGES` | Necessária para listar e inspecionar todos os apps instalados no dispositivo (inclusive os de sistema) |

Nenhum dado é coletado, enviado para servidores ou armazenado além do dispositivo local.

---

## Contribuindo

Contribuições são muito bem-vindas! Se você encontrou um falso positivo, tem sugestão de nova heurística ou quer melhorar a UI, fique à vontade para abrir uma *issue* ou enviar um *pull request*.

1. Faça um fork do projeto
2. Crie sua branch: `git checkout -b feature/minha-melhoria`
3. Commit suas mudanças: `git commit -m 'feat: minha melhoria'`
4. Push para a branch: `git push origin feature/minha-melhoria`
5. Abra um Pull Request

---

## Filtro de apps pessoais
 
A tela principal conta com um switch **"Mostrar apps pessoais"** que, quando desativado, oculta da listagem os aplicativos cujo pacote começa com `com.projetos.filmei`. Esse filtro existe porque o projeto foi desenvolvido pelo próprio autor, que preferiu ter a opção de esconder seus apps da lista durante demonstrações ou capturas de tela.
 
A lógica está no `MainViewModel`, dentro do bloco `when` que resolve o estado da tela:
 
```kotlin
when {
    error != null -> AppListState.Error(error)
    loading -> AppListState.Loading
    else -> {
        val filteredApps = if (mostrarAppsPessoais) {
            apps
        } else {
            apps.filter { !it.nomePacote.startsWith("com.projetos.filmei") }
        }
        AppListState.Success(filteredApps)
    }
}
```
 
> 💡 **Esse código é completamente opcional.** Se você fez um fork e não precisa desse comportamento, pode remover com segurança o bloco `filter`, o estado `mostrarAppsPessoais` e o switch correspondente na `HomeScreen`. O app continuará funcionando normalmente, exibindo todos os aplicativos instalados sem nenhuma exceção.
 
---

## Limitações conhecidas

- Apps com código fortemente ofuscado podem não ser identificados corretamente
- Alguns apps híbridos que combinam múltiplas tecnologias podem ser classificados de forma parcial
- A detecção de Kotlin Multiplatform ainda é experimental e pode não cobrir todos os padrões de build
- A pasta `res/layout/` é um indicador fraco quando usado isoladamente; o app tenta compensar isso com heurísticas adicionais

---

## Licença

Este projeto está licenciado sob a MIT License — sinta-se livre para usar, modificar e distribuir.

---

## Autor

Feito com ☕ e Kotlin.  
Contribuições, estrelinhas e feedbacks são sempre bem-vindos! 🌟
