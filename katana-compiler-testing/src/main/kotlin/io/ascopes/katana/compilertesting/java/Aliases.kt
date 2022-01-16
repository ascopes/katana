package io.ascopes.katana.compilertesting.java

import javax.tools.Diagnostic
import javax.tools.DiagnosticListener
import javax.tools.JavaFileObject

typealias DiagnosticListenerImpl = DiagnosticListener<JavaFileObject>
typealias DiagnosticImpl = Diagnostic<out JavaFileObject>
typealias JavaRamDiagnosticImpl = JavaRamDiagnostic<out JavaFileObject>
