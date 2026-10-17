package com.example.samplecoupon

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RestController

private const val ROOT = "com.example.samplecoupon"

class ArchitectureSpec :
    BehaviorSpec(
        {
          Given("production classes") {
            val classes =
                ClassFileImporter()
                    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                    .importPackages(ROOT)

            Then("domain does not depend on adapter, infra or usecase") {
              noClasses()
                  .that()
                  .resideInAPackage("$ROOT.domain..")
                  .should()
                  .dependOnClassesThat()
                  .resideInAnyPackage("$ROOT.adapter..", "$ROOT.infra..", "$ROOT.usecase..")
                  .check(classes)
            }

            Then("usecase does not depend on adapter or infra") {
              noClasses()
                  .that()
                  .resideInAPackage("$ROOT.usecase..")
                  .should()
                  .dependOnClassesThat()
                  .resideInAnyPackage("$ROOT.adapter..", "$ROOT.infra..")
                  .check(classes)
            }

            Then("infra does not depend on adapter or usecase") {
              noClasses()
                  .that()
                  .resideInAPackage("$ROOT.infra..")
                  .should()
                  .dependOnClassesThat()
                  .resideInAnyPackage("$ROOT.adapter..", "$ROOT.usecase..")
                  .check(classes)
            }

            Then("adapter does not depend on infra") {
              noClasses()
                  .that()
                  .resideInAPackage("$ROOT.adapter..")
                  .should()
                  .dependOnClassesThat()
                  .resideInAnyPackage("$ROOT.infra..")
                  .check(classes)
            }

            Then("common does not depend on domain, usecase, infra or adapter") {
              noClasses()
                  .that()
                  .resideInAPackage("$ROOT.common..")
                  .should()
                  .dependOnClassesThat()
                  .resideInAnyPackage(
                      "$ROOT.domain..",
                      "$ROOT.usecase..",
                      "$ROOT.infra..",
                      "$ROOT.adapter..",
                  )
                  .check(classes)
            }

            Then("use case services reside in the usecase package") {
              classes()
                  .that()
                  .areAnnotatedWith<Service>()
                  .should()
                  .resideInAPackage("$ROOT.usecase..")
                  .check(classes)
            }

            Then("controllers reside in the adapter rest package") {
              classes()
                  .that()
                  .areAnnotatedWith<RestController>()
                  .or()
                  .areAnnotatedWith<Controller>()
                  .should()
                  .resideInAPackage("$ROOT.adapter.rest..")
                  .check(classes)
            }

            Then("spring configuration classes reside in the infra package") {
              classes()
                  .that()
                  .areAnnotatedWith<Configuration>()
                  .should()
                  .resideInAPackage("$ROOT.infra..")
                  .check(classes)
            }
          }
        },
    )
