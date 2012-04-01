Struts 1.x tiles framework (or tiles standalone) is implementing the composite pattern for component based web pages. This is an old library for a technology that is not used very much today, but it has been very useful for legacy projects I have worked that still use struts 1.x.

The original framework had two limitations:
- Configuration for tiles was loaded only when the servlet was starting, making it a "compile time" configuraion
- The model is not allowing inheritance for components, making it very complicated to create complex sites

This project provides a new factory that overcomes both limitations:
- Provides API for changing configuration (selecting component to display) at runtime, at servlet, session or request scope.
- Implements inheritance for tiles

The project contains two modules, the library with the code and a war package with an example web application
