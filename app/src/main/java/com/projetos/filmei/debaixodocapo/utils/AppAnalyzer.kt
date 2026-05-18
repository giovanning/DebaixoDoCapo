package com.projetos.filmei.debaixodocapo.utils

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.zip.ZipFile

enum class AppFramework {
    NATIVE, FLUTTER, REACT_NATIVE, UNKNOWN
}

data class DetalhesAppNativos(
    val hasXml: Boolean = false,
    val hasCompose: Boolean = false,
    val isKmp: Boolean = false
)

data class AplicativoInstalado(
    val nome: String,
    val nomePacote: String,
    val framework: AppFramework,
    val detalhesNativos: DetalhesAppNativos? = null
)

class AppAnalyzer(private val packageManager: PackageManager) {

    suspend fun analisarAplicativosInstalados(): List<AplicativoInstalado> =
        withContext(Dispatchers.IO) {
            val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

            packages.mapNotNull { appInfo ->
                // Filtra apenas apps do usuário, excluindo os apps do sistema
                if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                    return@mapNotNull null
                }

                val nomeApp = packageManager.getApplicationLabel(appInfo).toString()
                val nomePacote = appInfo.packageName
                val caminhoApk = appInfo.sourceDir

                detectarFramework(
                    nomeApp = nomeApp,
                    nomePacote = nomePacote,
                    caminhoApk = caminhoApk
                )
            }.sortedBy { it.nome }
        }

    private fun detectarFramework(
        nomeApp: String,
        nomePacote: String,
        caminhoApk: String?
    ): AplicativoInstalado {
        if (caminhoApk == null) return AplicativoInstalado(
            nomeApp,
            nomePacote,
            AppFramework.UNKNOWN
        )

        try {
            ZipFile(caminhoApk).use { zip ->
                val entries = zip.entries()

                var hasReactNative = false
                var hasFlutter = false

                var hasXml = false
                var hasCompose = false
                var isKmp = false

                while (entries.hasMoreElements()) {
                    val entryName = entries.nextElement().name

                    when {
                        entryName.contains("libflutter.so") || entryName.startsWith("assets/flutter_assets") -> hasFlutter =
                            true

                        entryName.contains("libreactnativejni.so") || entryName.endsWith("index.android.bundle") -> hasReactNative =
                            true
                    }

                    when {
                        entryName.startsWith("res/layout/") -> hasXml = true
                        entryName.contains("androidx.compose") -> hasCompose = true

                        entryName.contains("shared.kotlin_module") ||
                                entryName.contains("kmp") ||
                                entryName.contains("kotlin-multiplatform") -> isKmp = true
                    }
                }

                val framework = when {
                    hasFlutter -> AppFramework.FLUTTER
                    hasReactNative -> AppFramework.REACT_NATIVE
                    else -> AppFramework.NATIVE
                }

                val nativeDetails = if (framework == AppFramework.NATIVE) {
                    DetalhesAppNativos(hasXml, hasCompose, isKmp)
                } else null

                return AplicativoInstalado(nomeApp, nomePacote, framework, nativeDetails)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return AplicativoInstalado(nomeApp, nomePacote, AppFramework.UNKNOWN)
        }
    }
}
