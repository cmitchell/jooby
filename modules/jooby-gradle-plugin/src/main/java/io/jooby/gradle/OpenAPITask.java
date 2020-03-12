/**
 * Jooby https://jooby.io
 * Apache License Version 2.0 https://jooby.io/LICENSE.txt
 * Copyright 2014 Edgar Espina
 */
package io.jooby.gradle;

import io.jooby.openapi.OpenAPIGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class OpenAPITask extends BaseTask {

  private String mainClassName;

  private String format = "json,yaml";

  private String includes;

  private String excludes;

  @TaskAction
  public void generate() throws Throwable {
    List<Project> projects = getProjects();

    String mainClass = Optional.ofNullable(mainClassName)
        .orElseGet(() -> computeMainClassName(projects));

    ClassLoader classLoader = createClassLoader(projects);

    getLogger().info(" Generating OpenAPI: " + mainClass);

    getLogger().debug("Using classloader: " + classLoader);

    String[] names = mainClass.split("\\.");
    Path dir = Stream.of(names)
        .reduce(classes(getProject()), Path::resolve, Path::resolve)
        .getParent();

    OpenAPIGenerator tool = new OpenAPIGenerator();
    tool.setClassLoader(classLoader);
    tool.setOutputDir(dir);
    trim(includes).ifPresent(tool::setIncludes);
    trim(excludes).ifPresent(tool::setExcludes);

    OpenAPI result = tool.generate(mainClass);

    for (OpenAPIGenerator.Format format : OpenAPIGenerator.Format.parse(format)) {
      tool.export(result, format);
    }
  }

  public String getMainClassName() {
    return mainClassName;
  }

  public void setMainClassName(String mainClassName) {
    this.mainClassName = mainClassName;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getIncludes() {
    return includes;
  }

  public void setIncludes(String includes) {
    this.includes = includes;
  }

  public String getExcludes() {
    return excludes;
  }

  public void setExcludes(String excludes) {
    this.excludes = excludes;
  }

  private Optional<String> trim(String value) {
    if (value == null || value.trim().length() == 0) {
      return Optional.empty();
    }
    return Optional.of(value.trim());
  }
}
