package com.dipien.byebyejetifier.scanner

import com.dipien.byebyejetifier.core.TypeRewriter
import com.dipien.byebyejetifier.core.config.Config

class ScannerContext(val config: Config) {

    val typeRewriter: TypeRewriter = TypeRewriter(config)
}
