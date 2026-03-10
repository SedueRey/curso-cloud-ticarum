package es.um.atica.umufly;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import jakarta.persistence.Entity;
import jakarta.validation.Valid;

@AnalyzeClasses(
  packages = "es.um.atica.umufly",
  importOptions = { ImportOption.DoNotIncludeTests.class }
)
public class UmuFlyArchitectureTests {

  @ArchTest
  static final ArchRule ninguna_interfaz_acaba_en_impl = noClasses().that().areInterfaces().should()
      .haveSimpleNameEndingWith("Impl")
      .because("Las interfaces no deben acabar por Impl");

  @ArchTest
  static final ArchRule codigo_respeta_arquitectura_hexagonal = layeredArchitecture()
      .consideringAllDependencies()
      .layer("Domain").definedBy("..domain..")
      .layer("Application").definedBy("..application..")
      // Soporta ambas variantes para evitar errores por naming inconsistente
      .layer("Adapters").definedBy("..adapters..", "..adaptors..")
      .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Adapters")
      .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapters")
      .whereLayer("Adapters").mayNotBeAccessedByAnyLayer();

  private static final ArchCondition<JavaMethod> METODO_REST_VALIDA_PARAMETROS = new ArchCondition<>(
      "Rest debe tener @Valid o @Validated en parámetros @RequestBody") {
    @Override
    public void check(JavaMethod metodo, ConditionEvents events) {
      for (JavaParameter parametro : metodo.getParameters()) {
        if (!parametro.isAnnotatedWith(RequestBody.class)) {
          continue;
        }

        boolean validaParametro = parametro.isAnnotatedWith(Valid.class)
            || parametro.isAnnotatedWith(Validated.class)
            || metodo.isAnnotatedWith(Validated.class)
            || metodo.getOwner().isAnnotatedWith(Validated.class);

        if (!validaParametro) {
          String message = String.format(
              "El método %s tiene un @RequestBody sin validación",
              metodo.getFullName());
          events.add(SimpleConditionEvent.violated(metodo, message));
        }
      }
    }
  };

  @ArchTest
  static final ArchRule api_rest_debe_validar_datos_entrada = methods()
      .that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
      .and().arePublic()
      .should(METODO_REST_VALIDA_PARAMETROS);

  private static final ArchCondition<JavaClass> MAXIMO_20_METODOS_PUBLICOS = new ArchCondition<>(
      "tener como máximo 20 métodos públicos") {
    @Override
    public void check(JavaClass clase, ConditionEvents events) {
      long metodosPublicosDeclarados = clase.getMethods().stream()
          .filter(metodo -> metodo.getModifiers().contains(JavaModifier.PUBLIC))
          .filter(metodo -> metodo.getOwner().equals(clase))
          .count();

      if (metodosPublicosDeclarados > 20) {
        String message = String.format(
            "La clase %s tiene %d métodos públicos (máximo permitido: 20)",
            clase.getFullName(),
            metodosPublicosDeclarados);
        events.add(SimpleConditionEvent.violated(clase, message));
      }
    }
  };

  @ArchTest
  static final ArchRule ninguna_clase_debe_tener_mas_de_20_metodos_publicos = classes()
      .that().areNotAnnotatedWith(Entity.class)
      .should(MAXIMO_20_METODOS_PUBLICOS)
      .because("Ninguna clase debe tener más de 20 métodos públicos");

    @ArchTest
    static final ArchRule el_nombre_de_los_dto_acaban_en_dto = classes()
      .that().resideInAPackage("..dto..")
      .and().areNotEnums()
      .and().areNotInterfaces()
      .should().haveSimpleNameEndingWith("DTO")
      .because("El nombre de los DTO debe acabar en DTO");

  @ArchTest
  static final ArchRule los_restcontroller_solo_pueden_estar_en_capa_adaptadores = classes()
      .that().areAnnotatedWith(RestController.class)
      .should().resideInAnyPackage("..adapters..", "..adaptors..")
      .because("Los RestController solo pueden estar en la capa de adaptadores");
  
  
}