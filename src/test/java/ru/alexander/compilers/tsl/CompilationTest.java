package ru.alexander.compilers.tsl;

import junit.framework.TestCase;
import ru.alexander.compilers.tsl.data.TSCode;

public class CompilationTest extends TestCase {
    public void testCompilation() {
        TSLCompiler compiler = new TSLCompiler();
        String code = """
                          def vtex() {
                            h = 10.66;                         
                            lambda = pow(77, 2) - (5 + 3) * 3;
                            list[10];
                            
                            for (i = 0; i < 10; i += 1) {
                                list[i] = 0;
                                h += 7;
                            }
                            list[9] = 7;
                            
                            typewriter(h);
                            world(h, lambda);
                            delta = (h - lambda) / 2;
                          }
                          
                          def litex() {
                            tetta = 0;
                            lambda -= 10;
                            world(tetta, lambda);
                            typewriter(lambda);
                          
                          }
                          
                          def world(mirror, reality) {
                            reality *= mirror - 3;
                          }
                          
                          def typewriter(info) {
                            info /= 2;
                          }
                        """;


        TSCode[] compile = compiler.compile(code);
    }
}
