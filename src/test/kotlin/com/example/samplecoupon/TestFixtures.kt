package com.example.samplecoupon

import com.tngtech.archunit.lang.syntax.elements.ClassesThat
import com.tngtech.archunit.lang.syntax.elements.GivenClassesConjunction
import io.kotest.core.spec.Spec

/**
 * Runs [block] before each root container (`Given`). Leaf-level `beforeEach` would fire only before
 * `Then`, i.e. after the `Given`/`When` bodies have already arranged and acted.
 */
fun Spec.beforeEachGiven(block: suspend () -> Unit) {
  beforeContainer { if (it.parent == null) block() }
}

/** Reified shorthand for [ClassesThat.areAnnotatedWith], e.g. `areAnnotatedWith<Service>()`. */
inline fun <reified A : Annotation> ClassesThat<GivenClassesConjunction>.areAnnotatedWith():
    GivenClassesConjunction = areAnnotatedWith(A::class.java)
