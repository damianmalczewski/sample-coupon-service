package com.example.samplecoupon

import io.kotest.core.spec.Spec

/**
 * Runs [block] before each root container (`Given`). Leaf-level `beforeEach` would fire only before
 * `Then`, i.e. after the `Given`/`When` bodies have already arranged and acted.
 */
fun Spec.beforeEachGiven(block: suspend () -> Unit) {
  beforeContainer { if (it.parent == null) block() }
}
