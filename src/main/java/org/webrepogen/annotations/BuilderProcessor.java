package org.webrepogen.annotations;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import lombok.ToString;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@SupportedAnnotationTypes({
        "org.webrepogen.annotations.*",
})
@AutoService(Processor.class)
public class BuilderProcessor extends AbstractProcessor {

    private static final String WEB = "web";
    private static final String REPO = "repository";
    private static final String SERVICE = "service";

    private void writeToFile(String name, String targetPackage, Consumer<ProcessingWriter> consumer) {
        JavaFileObject builderFile = null;
        try {
            builderFile = processingEnv.getFiler()
                    .createSourceFile(subpackage(targetPackage, name));

            try (ProcessingWriter out = new ProcessingWriter(new PrintWriter(builderFile.openWriter()))) {
                consumer.accept(out);
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Cannot create output file");
        }

    }

    private void printStaticController(String targetPackage, String name, List<String> types) {
        String nName = name + "Controller";
        writeToFile(nName, targetPackage, (out) -> {
            out.println("package " + targetPackage + ";");
            out.importType("org.springframework.web.bind.annotation.RestController");
            out.importType("org.springframework.web.bind.annotation.RequestMapping");
            out.importType("org.springframework.http.MediaType");
            out.importType("org.springframework.web.bind.annotation.RequestMethod");
            out.importType("java.util.Arrays");
            out.importType("java.util.List");
            out.println();

            out.println("@RestController");
            out.println("@RequestMapping(\"/api\")");
            out.println("public class " + nName + " {");
            out.println("\t@RequestMapping(value=\"/description\",method=RequestMethod.GET,produces=MediaType.APPLICATION_JSON_VALUE)");
            out.println("\tpublic List<String> description() {");
            out.println("\t\treturn Arrays.asList(" + types.stream().map(type -> "\"" + type + "\"").collect(Collectors.joining(",")) + ");");
            out.println("\t}");
            out.println("}");
        });
    }

    private void printController(String targetPackage, String name, String type, String typeSimple, String idType, String controllerBase, String repositoryBase) {
        String nName = name + "Controller";
        writeToFile(nName, targetPackage, (out) -> {
            out.println("package " + targetPackage + ";");
            out.importType("org.springframework.web.bind.annotation.RestController");
            out.importType("org.springframework.web.bind.annotation.RequestMapping");
            out.importType("org.springframework.beans.factory.annotation.Autowired");
            String nType = out.importType(type);
            String nIdType = out.importType(idType);
            String nControllerBase = out.importType(controllerBase);
            String nRepositoryBase = out.importType(repositoryBase);
            out.println();

            out.println("@RestController");
            out.println("@RequestMapping(\"/api/" + typeSimple.toLowerCase() + "\")");
            out.println("public class " + nName + " extends " + nControllerBase + "<" + nType + ", " + nIdType + "> {");
            out.println("\t@Autowired");
            out.println("\tpublic " + nName + "(" + nRepositoryBase + "<" + nType + ", " + nIdType + "> repository) {");
            out.println("\t\tinit(repository, " + nType + ".class, " + nIdType + ".class);");
            out.println("\t}");
            out.println("}");
        });
    }

    private void printRepo(String targetPackage, String name, String type, String typeSimple, String idType, String repositoryBase) {
        String nName = name + "Repository";
        writeToFile(nName, targetPackage, (out) -> {
            out.println("package " + targetPackage + ";");
            String nType = out.importType(type);
            String nIdType = out.importType(idType);
            String nRepositoryBase = out.importType(repositoryBase);
            out.importType("org.springframework.stereotype.Repository;");
            out.println();

            out.println("@Repository");
            out.println("public interface " + nName + " extends " + nRepositoryBase + "<" + nType + ", " + nIdType + "> {}");
        });
    }

    private void printService(String targetPackage, String name, String type, String typeSimple, String idType) {

    }

    private String getSinglePackage(Class<? extends Annotation> annotation, RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedElements
                = roundEnv.getElementsAnnotatedWith(annotation);
        if (annotatedElements.size() > 1) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "There can be only one WebRepositoryPackage");
        } else if (annotatedElements.size() < 1) {
            return null;
        }
        return processingEnv.getElementUtils().getPackageOf(annotatedElements.iterator().next()).getQualifiedName().toString();
    }

    private String getPrioritized(String... args) {
        int i = 0;
        for (String string : args) {
            if (string != null && !string.isBlank()) {
                return string;
            }
        }
        return null;
    }

    private String subpackage(String package1, String subpackage) {
        return package1 + "." + subpackage;
    }

    private <T extends Annotation> T getSingleAnnotation(Class<T> annotation, RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedElements
                = roundEnv.getElementsAnnotatedWith(annotation);
        if (annotatedElements.size() >= 1) {
            return annotatedElements.iterator().next().getAnnotation(annotation);
        }
        return null;
    }

    private <T> T getDefaultAnnotationValue(Class<? extends Annotation> annotation, String name, Class<T> returnType) {
        Method method = null;
        try {
            method = annotation.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return (T) method.getDefaultValue();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private List<? extends Element> getIdsRecursive(TypeElement element) {
        if(element == null) {
            return ImmutableList.of();
        }
        List<? extends Element> ids = processingEnv.getElementUtils()
                .getAllMembers(element).stream().filter(member -> member.getAnnotation(Id.class) != null).collect(Collectors.toList());
        if(ids.size() == 0) {
            return getIdsRecursive((TypeElement) processingEnv.getTypeUtils().asElement((element).getSuperclass()));
        }
        return ids;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        Class<? extends Annotation> generateAnnotationClass = GenerateWebRepository.class;
        if (roundEnv.getElementsAnnotatedWith(AllEntities.class).size() > 0) {
            generateAnnotationClass = Entity.class;
        }

        String webPackage = getSinglePackage(WebPackage.class, roundEnv);
        String repoPackage = getSinglePackage(RepoPackage.class, roundEnv);
        String servicePackage = getSinglePackage(ServicePackage.class, roundEnv);
        System.out.println("Configuring WR");

        WRConfiguration wrConfiguration = getSingleAnnotation(WRConfiguration.class, roundEnv);

        String controllerBase = getDefaultAnnotationValue(WRConfiguration.class, "controllerBaseClass", String.class);
        String repositoryBase = getDefaultAnnotationValue(WRConfiguration.class, "repositoryBaseInterface", String.class);
        if (wrConfiguration != null) {
            controllerBase = wrConfiguration.controllerBaseClass();
            repositoryBase = wrConfiguration.repositoryBaseInterface();
        }
        System.out.println("WR configured");
        List<String> types = new ArrayList<>();
        Set<? extends Element> elements = ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(generateAnnotationClass));
        for (Element element : elements) {
            System.out.println(element);
            if (element.getKind() == ElementKind.CLASS && element.getAnnotation(Entity.class) != null && element.getAnnotation(ExcludedEntity.class) == null) {
                PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(element);
                String defaultPackage = packageElement.getQualifiedName().toString();
                String forcePackage = "";
                GenerateWebRepository genInfo = element.getAnnotation(GenerateWebRepository.class);
                if (genInfo != null)
                    forcePackage = element.getAnnotation(GenerateWebRepository.class).targetPackage();
                String targetRepoPackage = subpackage(getPrioritized(forcePackage, repoPackage, defaultPackage), REPO);
                String idTypeName;
                String elementName = element.getSimpleName().toString();
                String elementTypeName = element.toString();
                String typeSimple = element.getSimpleName().toString();
                List<? extends Element> ids = getIdsRecursive((TypeElement) element);
                if (ids.size() == 1) {
                    idTypeName = ids.get(0).asType().toString();
                } else {
                    System.out.println("X");
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Wrong number of ids");
                    continue;
                }
                System.out.println("Generating code for " + elementName);
                printRepo(targetRepoPackage, elementName, elementTypeName, typeSimple, idTypeName, repositoryBase);
                String targetWebPackage = subpackage(getPrioritized(forcePackage, repoPackage, defaultPackage), WEB);
                types.add(typeSimple);
                printController(targetWebPackage, elementName, elementTypeName, typeSimple, idTypeName, controllerBase, repositoryBase);
            }
        }
        printStaticController(subpackage(repoPackage, WEB), "Static", types);

        return true;
    }
}
