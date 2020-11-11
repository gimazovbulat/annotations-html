package ru.itis.html.generator;

import com.google.auto.service.AutoService;
import freemarker.template.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes(value = {"ru.itis.html.generator.HtmlForm"})
public class HtmlProcessor extends AbstractProcessor {
    private final Configuration configuration;

    public HtmlProcessor() {
        this.configuration = configureFreemarker();
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // get types with "HtmlForm" annotation
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(HtmlForm.class);
        // get path with class-file
        String path = File.separator + HtmlProcessor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            configuration.setDirectoryForTemplateLoading(new File(path));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        Map<String, Object> formParams = new HashMap<>();
        for (Element element : annotatedElements) {
            // create path to html-file
            String resultHtmlFile = path.substring(1) + element.getSimpleName().toString().toLowerCase() + ".html";
            Path out = Paths.get(resultHtmlFile);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(out.toFile()));
                 StringWriter templInMemWriter = new StringWriter()) {
                // put into map html-form information
                HtmlForm htmlFormAnnotation = element.getAnnotation(HtmlForm.class);
                formParams.put("action", htmlFormAnnotation.action());
                formParams.put("method", htmlFormAnnotation.method());
                // put into map html-input information
                List<? extends Element> elements = element.getEnclosedElements();
                formParams.putAll(htmlInputMap(elements));
                // replace keys in template
                Template template = configuration.getTemplate(element.getSimpleName().toString().toLowerCase() + ".ftl");
                template.process(formParams, templInMemWriter);
                writer.write(templInMemWriter.toString());
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            } catch (TemplateException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private Map<String, Object> htmlInputMap(List<? extends Element> elements) {
        return new HashMap<>() {{
            put("formInputs", elements.stream().filter(element -> element.getAnnotation(HtmlInput.class) != null)
                    .map(element -> {
                        return FormInput.builder()
                                .name(element.getAnnotation(HtmlInput.class).name())
                                .placeholder(element.getAnnotation(HtmlInput.class).placeholder())
                                .type(element.getAnnotation(HtmlInput.class).type())
                                .build();
                    })
                    .collect(Collectors.toList()));
        }};
    }

    private Configuration configureFreemarker() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
        DefaultObjectWrapperBuilder owb = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_30);
        owb.setIterableSupport(true);
        cfg.setObjectWrapper(owb.build());
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        return cfg;
    }
}