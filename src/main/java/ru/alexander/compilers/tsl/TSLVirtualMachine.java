package ru.alexander.compilers.tsl;

import org.jetbrains.annotations.NotNull;
import ru.alexander.compilers.exception.VMException;
import ru.alexander.compilers.tsl.data.TSCode;

public class TSLVirtualMachine {
    private final float[] variables;
    private final Vector2[] vector2Var;
    private final Vector3[] vector3Var;
    private final TSCode code;

    public TSLVirtualMachine(TSCode code) {
        variables = new float[code.varBuffSize];
        vector2Var = new Vector2[code.vec2BuffSize];
        vector3Var = new Vector3[code.vec3BuffSize];

        this.code = code;
    }

    public void runCode() {
        int pointer = 0;
        runner(code.script, pointer);
    }

    public static final int mathShift = 10;
    public static final int vecMathShift = mathShift + 17;
    public static final int funcShift = vecMathShift + 9;
    private int runner(float @NotNull [] code, int pointer) {
        while (pointer < code.length) {
            switch ((int) code[pointer]) {
                case 1 -> {
                    int i = (int) code[pointer + 1];
                    if (i >= variables.length) {
                        i -= variables.length;
                        if (i >= vector2Var.length) {
                            i -= vector2Var.length;
                            if (i >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else {
                                float x = code[pointer + 2];
                                float y = code[pointer + 3];
                                float z = code[pointer + 4];
                                vector3Var[i] = new Vector3(x, y, z);
                                pointer += 5;
                            }
                        } else {
                            float x = code[pointer + 2];
                            float y = code[pointer + 3];
                            vector2Var[i] = new Vector2(x, y);
                            pointer += 4;
                        }
                    } else {
                        float var = code[pointer + 2];
                        variables[i] = var;
                        pointer += 3;
                    }
                } //set
                case 2 -> {
                    int i = (int) (code[pointer + 1] + variables[(int) code[pointer + 2]]);
                    int from = (int) (code[pointer + 3]);
                    if (i >= variables.length) {
                        i -= variables.length;
                        from -= variables.length;
                        if (i >= vector2Var.length) {
                            i -= vector2Var.length;
                            from -= vector2Var.length;
                            if (i >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else vector3Var[i] = vector3Var[from];
                        } else vector2Var[i] = vector2Var[from];
                    } else variables[i] = variables[from];
                    pointer += 4;
                } //seta
                case 3 -> {
                    int i = (int) (code[pointer + 2] + variables[(int) code[pointer + 3]]);
                    int to = (int) (code[pointer + 1]);
                    if (i >= variables.length) {
                        i -= variables.length;
                        to -= variables.length;
                        if (i >= vector2Var.length) {
                            i -= vector2Var.length;
                            to -= vector2Var.length;
                            if (i >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else vector3Var[to] = vector3Var[i];
                        } else vector2Var[to] = vector2Var[i];
                    } else variables[to] = variables[i];
                    pointer += 4;
                } //geta
                case 4 -> {
                    int np = (int) code[pointer + 1];
                    int op = (int) code[pointer + 2];
                    if (op >= variables.length) {
                        op -= variables.length;
                        np -= variables.length;
                        if (op >= vector2Var.length) {
                            op -= vector2Var.length;
                            np -= vector2Var.length;
                            if (op >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else {
                                try {
                                    vector3Var[np] = vector3Var[op];
                                } catch (Exception e) {
                                    throw new VMException("TSL", "memory heap overflow");
                                }
                            }
                        } else {
                            try {
                                vector2Var[np] = vector2Var[op];
                            } catch (Exception e) {
                                throw new VMException("TSL", "memory heap overflow");
                            }
                        }
                    } else {
                        try {
                            variables[np] = variables[op];
                        } catch (Exception e) {
                            throw new VMException("TSL", "memory heap overflow");
                        }
                    }
                    pointer += 3;
                } //mov

                case 5 -> {
                    int vecI = (int) code[pointer + 1];
                    int i = (int) code[pointer + 2];
                    if (vecI >= variables.length && i < variables.length) {
                        vecI -= variables.length;
                        if (vecI >= vector2Var.length) {
                            vecI -= vector2Var.length;
                            if (vecI >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else vector3Var[vecI].x = variables[i];

                        }
                        else vector2Var[vecI].x = variables[i];
                    }
                    else throw new VMException("TSL", "incorrect data");

                    pointer += 3;
                } //svx
                case 6 -> {
                    int vecI = (int) code[pointer + 1];
                    int i = (int) code[pointer + 2];
                    if (vecI >= variables.length && i < variables.length) {
                        vecI -= variables.length;
                        if (vecI >= vector2Var.length) {
                            vecI -= vector2Var.length;
                            if (vecI >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else  vector3Var[vecI].y = variables[i];

                        }
                        else vector2Var[vecI].y = variables[i];
                    }
                    else throw new VMException("TSL", "incorrect data");

                    pointer += 3;
                } //svy
                case 7 -> {
                    int i = (int) code[pointer + 1];
                    int vecI = (int) code[pointer + 2];
                    vecI -= variables.length;
                    if (vecI >= vector2Var.length && i < variables.length) {
                        vecI -= vector2Var.length;
                        if (vecI >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                        else vector3Var[vecI].z = variables[i];

                    } else throw new VMException("TSL", "incorrect data");
                    pointer += 3;
                } //svz

                case 8 -> {
                    int i = (int) code[pointer + 1];
                    int vecI = (int) code[pointer + 2];
                    if (vecI >= variables.length && i < variables.length) {
                        vecI -= variables.length;
                        if (vecI >= vector2Var.length) {
                            vecI -= vector2Var.length;
                            if (vecI >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else variables[i] = vector3Var[vecI].x;

                        }
                        else variables[i] = vector2Var[vecI].x;
                    }
                    else throw new VMException("TSL", "incorrect data");

                    pointer += 3;
                } //gvx
                case 9 -> {
                    int i = (int) code[pointer + 1];
                    int vecI = (int) code[pointer + 2];
                    if (vecI >= variables.length && i < variables.length) {
                        vecI -= variables.length;
                        if (vecI >= vector2Var.length) {
                            vecI -= vector2Var.length;
                            if (vecI >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else variables[i] = vector3Var[vecI].y;

                        }
                        else variables[i] = vector2Var[vecI].y;
                    }
                    else throw new VMException("TSL", "incorrect data");

                    pointer += 3;
                } //gvy
                case 10 -> {
                    int i = (int) code[pointer + 1];
                    int vecI = (int) code[pointer + 2];
                    vecI -= variables.length;
                    if (vecI >= vector2Var.length && i < variables.length) {
                        vecI -= vector2Var.length;
                        if (vecI >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                        else variables[i] = vector3Var[vecI].z;

                    } else throw new VMException("TSL", "incorrect data");
                    pointer += 3;
                } //gvz

                case mathShift + 1 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    int c = (int) code[pointer + 3];
                    if (a >= variables.length) {
                        a -= variables.length;
                        b -= variables.length;
                        c -= variables.length;
                        if (a >= vector2Var.length) {
                            a -= vector2Var.length;
                            b -= vector2Var.length;
                            c -= vector2Var.length;
                            if (a >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else {
                                try {
                                    vector3Var[a] = new Vector3(
                                            vector3Var[b].x + vector3Var[c].x,
                                            vector3Var[b].y + vector3Var[c].y,
                                            vector3Var[b].z + vector3Var[c].z);
                                } catch (Exception e) {
                                    throw new VMException("TSL", "error in vec3 math");
                                }
                            }
                        } else {
                            try {
                                vector2Var[a] = new Vector2(
                                        vector2Var[b].x + vector2Var[c].x,
                                        vector2Var[b].y + vector2Var[c].y);
                            } catch (Exception e) {
                                throw new VMException("TSL", "error in vec2 math");
                            }
                        }
                    } else {
                        try {
                            variables[a] = variables[b] + variables[c];
                        } catch (Exception e) {
                            throw new VMException("TSL", "error in var math");
                        }
                    }
                    pointer += 4;
                } //add
                case mathShift + 2 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    int c = (int) code[pointer + 3];
                    if (a >= variables.length) {
                        a -= variables.length;
                        b -= variables.length;
                        c -= variables.length;
                        if (a >= vector2Var.length) {
                            a -= vector2Var.length;
                            b -= vector2Var.length;
                            c -= vector2Var.length;
                            if (a >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else {
                                try {
                                    vector3Var[a] = new Vector3(
                                            vector3Var[b].x - vector3Var[c].x,
                                            vector3Var[b].y - vector3Var[c].y,
                                            vector3Var[b].z - vector3Var[c].z);
                                } catch (Exception e) {
                                    throw new VMException("TSL", "error in vec3 math");
                                }
                            }
                        } else {
                            try {
                                vector2Var[a] = new Vector2(
                                        vector2Var[b].x - vector2Var[c].x,
                                        vector2Var[b].y - vector2Var[c].y);
                            } catch (Exception e) {
                                throw new VMException("TSL", "error in vec2 math");
                            }
                        }
                    } else {
                        try {
                            variables[a] = variables[b] - variables[c];
                        } catch (Exception e) {
                            throw new VMException("TSL", "error in var math");
                        }
                    }
                    pointer += 4;
                } //sub
                case mathShift + 3 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    int c = (int) code[pointer + 3];
                    if (a >= variables.length) {
                        int d = c;
                        a -= variables.length;
                        b -= variables.length;
                        c -= variables.length;
                        if (a >= vector2Var.length) {
                            a -= vector2Var.length;
                            b -= vector2Var.length;
                            c -= vector2Var.length;
                            if (a >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else {
                                try {
                                    vector3Var[a] = new Vector3(
                                            vector3Var[b].x * vector3Var[c].x,
                                            vector3Var[b].y * vector3Var[c].y,
                                            vector3Var[b].z * vector3Var[c].z);
                                } catch (Exception e) {
                                    try {
                                        vector3Var[a] = new Vector3(
                                                vector3Var[b].x * variables[d],
                                                vector3Var[b].y * variables[d],
                                                vector3Var[b].z * variables[d]);
                                    } catch (Exception ex) {
                                        throw new VMException("TSL", "error in vec3 math");
                                    }
                                }
                            }
                        } else {
                            try {
                                vector2Var[a] = new Vector2(
                                        vector2Var[b].x * vector2Var[c].x,
                                        vector2Var[b].y * vector2Var[c].y);
                            } catch (Exception e) {
                                try {
                                    vector2Var[a] = new Vector2(
                                            vector2Var[b].x * variables[d],
                                            vector2Var[b].y * variables[d]);
                                } catch (Exception ex) {
                                    throw new VMException("TSL", "error in vec2 math");
                                }
                            }
                        }
                    } else {
                        try {
                            variables[a] = variables[b] * variables[c];
                        } catch (Exception e) {
                            throw new VMException("TSL", "error in var math");
                        }
                    }
                    pointer += 4;
                } //mul
                case mathShift + 4 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    int c = (int) code[pointer + 3];
                    if (a >= variables.length) {
                        a -= variables.length;
                        b -= variables.length;
                        c -= variables.length;
                        if (a >= vector2Var.length) {
                            a -= vector2Var.length;
                            b -= vector2Var.length;
                            c -= vector2Var.length;
                            if (a >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else {
                                try {
                                    vector3Var[a] = new Vector3(
                                            vector3Var[b].x / vector3Var[c].x,
                                            vector3Var[b].y / vector3Var[c].y,
                                            vector3Var[b].z / vector3Var[c].z);
                                } catch (Exception e) {
                                    try {
                                        vector3Var[a] = new Vector3(
                                                vector3Var[b].x / variables[c],
                                                vector3Var[b].y / variables[c],
                                                vector3Var[b].z / variables[c]);
                                    } catch (Exception ex) {
                                        throw new VMException("TSL", "error in vec3 math");
                                    }
                                }
                            }
                        } else {
                            try {
                                vector2Var[a] = new Vector2(
                                        vector2Var[b].x / vector2Var[c].x,
                                        vector2Var[b].y / vector2Var[c].y);
                            } catch (Exception e) {
                                try {
                                    vector2Var[a] = new Vector2(
                                            vector2Var[b].x / variables[c],
                                            vector2Var[b].y / variables[c]);
                                } catch (Exception ex) {
                                    throw new VMException("TSL", "error in vec2 math");
                                }
                            }
                        }
                    } else {
                        try {
                            variables[a] = variables[b] / variables[c];
                        } catch (Exception e) {
                            throw new VMException("TSL", "error in var math");
                        }
                    }
                    pointer += 4;
                } //div
                case mathShift + 5 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    if (a < variables.length) {
                        variables[a] = Math.abs(variables[b]);
                    } else throw new VMException("TSL", "error in var math");
                    pointer += 3;
                } //abs
                case mathShift + 6 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    int c = (int) code[pointer + 3];
                    if (a < variables.length) {
                        variables[a] = (float) Math.pow(variables[b], variables[c]);
                    } else throw new VMException("TSL", "error in var math");
                    pointer += 4;
                } //pow
                case mathShift + 7 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    if (a < variables.length) {
                        variables[a] = (float) Math.sqrt(variables[b]);
                    } else throw new VMException("TSL", "error in var math");
                    pointer += 3;
                } //sqrt
                case mathShift + 8 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    if (a < variables.length) {
                        variables[a] = (float) Math.log(variables[b]);
                    } else throw new VMException("TSL", "error in var math");
                    pointer += 3;
                } //log
                case mathShift + 9 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    if (a < variables.length) {
                        variables[a] = (float) Math.sin(variables[b]);
                    } else throw new VMException("TSL", "error in var math");
                    pointer += 3;
                } //sin
                case mathShift + 10 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    if (a < variables.length) {
                        variables[a] = (float) Math.cos(variables[b]);
                    } else throw new VMException("TSL", "error in var math");
                    pointer += 3;
                } //cos
                case mathShift + 11 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    if (a < variables.length) {
                        variables[a] = (float) Math.tan(variables[b]);
                    } else throw new VMException("TSL", "error in var math");
                    pointer += 3;
                } //tan
                case mathShift + 12 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    int c = (int) code[pointer + 3];
                    if (b >= variables.length) {
                        b -= variables.length;
                        c -= variables.length;
                        if (b >= vector2Var.length) {
                            b -= vector2Var.length;
                            c -= vector2Var.length;
                            if (b >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else {
                                try {
                                    Vector3 v3 = vector3Var[b];
                                    float l1 = (float) Math.sqrt(v3.x * v3.x + v3.y * v3.y + v3.z * v3.z);
                                    v3 = vector3Var[c];
                                    float l2 = (float) Math.sqrt(v3.x * v3.x + v3.y * v3.y + v3.z * v3.z);
                                    if (l1 > l2) variables[a] = 1;
                                    else variables[a] = 0;
                                } catch (Exception e) {
                                    throw new VMException("TSL", "error in vec3 math");
                                }
                            }
                        } else {
                            try {
                                Vector2 v2 = vector2Var[b];
                                float l1 = (float) Math.sqrt(v2.x * v2.x + v2.y * v2.y);
                                v2 = vector2Var[c];
                                float l2 = (float) Math.sqrt(v2.x * v2.x + v2.y * v2.y);
                                if (l1 > l2) variables[a] = 1;
                                else variables[a] = 0;
                            } catch (Exception e) {
                                throw new VMException("TSL", "error in vec2 math");
                            }
                        }
                    } else {
                        try {
                            if (variables[b] > variables[c]) variables[a] = 1;
                            else variables[a] = 0;
                        } catch (Exception e) {
                            throw new VMException("TSL", "error in var math");
                        }
                    }
                    pointer += 4;
                } //mt
                case mathShift + 13 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    int c = (int) code[pointer + 3];
                    if (b >= variables.length) {
                        b -= variables.length;
                        c -= variables.length;
                        if (b >= vector2Var.length) {
                            b -= vector2Var.length;
                            c -= vector2Var.length;
                            if (b >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else {
                                try {
                                    Vector3 v3 = vector3Var[b];
                                    float l1 = (float) Math.sqrt(v3.x * v3.x + v3.y * v3.y + v3.z * v3.z);
                                    v3 = vector3Var[c];
                                    float l2 = (float) Math.sqrt(v3.x * v3.x + v3.y * v3.y + v3.z * v3.z);
                                    if (l1 >= l2) variables[a] = 1;
                                    else variables[a] = 0;
                                } catch (Exception e) {
                                    throw new VMException("TSL", "error in vec3 math");
                                }
                            }
                        } else {
                            try {
                                Vector2 v2 = vector2Var[b];
                                float l1 = (float) Math.sqrt(v2.x * v2.x + v2.y * v2.y);
                                v2 = vector2Var[c];
                                float l2 = (float) Math.sqrt(v2.x * v2.x + v2.y * v2.y);
                                if (l1 >= l2) variables[a] = 1;
                                else variables[a] = 0;
                            } catch (Exception e) {
                                throw new VMException("TSL", "error in vec2 math");
                            }
                        }
                    } else {
                        try {
                            if (variables[b] >= variables[c]) variables[a] = 1;
                            else variables[a] = 0;
                        } catch (Exception e) {
                            throw new VMException("TSL", "error in var math");
                        }
                    }
                    pointer += 4;
                } //met
                case mathShift + 14 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    int c = (int) code[pointer + 3];
                    if (b >= variables.length) {
                        b -= variables.length;
                        c -= variables.length;
                        if (b >= vector2Var.length) {
                            b -= vector2Var.length;
                            c -= vector2Var.length;
                            if (b >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else {
                                try {
                                    Vector3 v3 = vector3Var[b];
                                    float l1 = (float) Math.sqrt(v3.x * v3.x + v3.y * v3.y + v3.z * v3.z);
                                    v3 = vector3Var[c];
                                    float l2 = (float) Math.sqrt(v3.x * v3.x + v3.y * v3.y + v3.z * v3.z);
                                    if (l1 < l2) variables[a] = 1;
                                    else variables[a] = 0;
                                } catch (Exception e) {
                                    throw new VMException("TSL", "error in vec3 math");
                                }
                            }
                        } else {
                            try {
                                Vector2 v2 = vector2Var[b];
                                float l1 = (float) Math.sqrt(v2.x * v2.x + v2.y * v2.y);
                                v2 = vector2Var[c];
                                float l2 = (float) Math.sqrt(v2.x * v2.x + v2.y * v2.y);
                                if (l1 < l2) variables[a] = 1;
                                else variables[a] = 0;
                            } catch (Exception e) {
                                throw new VMException("TSL", "error in vec2 math");
                            }
                        }
                    } else {
                        try {
                            if (variables[b] < variables[c]) variables[a] = 1;
                            else variables[a] = 0;
                        } catch (Exception e) {
                            throw new VMException("TSL", "error in var math");
                        }
                    }
                    pointer += 4;
                } //lt
                case mathShift + 15 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    int c = (int) code[pointer + 3];
                    if (b >= variables.length) {
                        b -= variables.length;
                        c -= variables.length;
                        if (b >= vector2Var.length) {
                            b -= vector2Var.length;
                            c -= vector2Var.length;
                            if (b >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else {
                                try {
                                    Vector3 v3 = vector3Var[b];
                                    float l1 = (float) Math.sqrt(v3.x * v3.x + v3.y * v3.y + v3.z * v3.z);
                                    v3 = vector3Var[c];
                                    float l2 = (float) Math.sqrt(v3.x * v3.x + v3.y * v3.y + v3.z * v3.z);
                                    if (l1 <= l2) variables[a] = 1;
                                    else variables[a] = 0;
                                } catch (Exception e) {
                                    throw new VMException("TSL", "error in vec3 math");
                                }
                            }
                        } else {
                            try {
                                Vector2 v2 = vector2Var[b];
                                float l1 = (float) Math.sqrt(v2.x * v2.x + v2.y * v2.y);
                                v2 = vector2Var[c];
                                float l2 = (float) Math.sqrt(v2.x * v2.x + v2.y * v2.y);
                                if (l1 <= l2) variables[a] = 1;
                                else variables[a] = 0;
                            } catch (Exception e) {
                                throw new VMException("TSL", "error in vec2 math");
                            }
                        }
                    } else {
                        try {
                            if (variables[b] <= variables[c]) variables[a] = 1;
                            else variables[a] = 0;
                        } catch (Exception e) {
                            throw new VMException("TSL", "error in var math");
                        }
                    }
                    pointer += 4;
                } //let
                case mathShift + 16 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    int c = (int) code[pointer + 3];
                    if (b >= variables.length) {
                        b -= variables.length;
                        c -= variables.length;
                        if (b >= vector2Var.length) {
                            b -= vector2Var.length;
                            c -= vector2Var.length;
                            if (b >= vector3Var.length) throw new VMException("TSL", "memory heap overflow");
                            else {
                                try {
                                    Vector3 v3 = vector3Var[b];
                                    float l1 = (float) Math.sqrt(v3.x * v3.x + v3.y * v3.y + v3.z * v3.z);
                                    v3 = vector3Var[c];
                                    float l2 = (float) Math.sqrt(v3.x * v3.x + v3.y * v3.y + v3.z * v3.z);
                                    if (l1 == l2) variables[a] = 1;
                                    else variables[a] = 0;
                                } catch (Exception e) {
                                    throw new VMException("TSL", "error in vec3 math");
                                }
                            }
                        } else {
                            try {
                                Vector2 v2 = vector2Var[b];
                                float l1 = (float) Math.sqrt(v2.x * v2.x + v2.y * v2.y);
                                v2 = vector2Var[c];
                                float l2 = (float) Math.sqrt(v2.x * v2.x + v2.y * v2.y);
                                if (l1 == l2) variables[a] = 1;
                                else variables[a] = 0;
                            } catch (Exception e) {
                                throw new VMException("TSL", "error in vec2 math");
                            }
                        }
                    } else {
                        try {
                            if (variables[b] == variables[c]) variables[a] = 1;
                            else variables[a] = 0;
                        } catch (Exception e) {
                            throw new VMException("TSL", "error in var math");
                        }
                    }
                    pointer += 4;
                } //eq
                case mathShift + 17 -> {
                    try {
                        float in = variables[(int) code[pointer + 2]];
                        float n = variables[(int) code[pointer + 3]];
                        if (in > n) variables[(int) code[pointer + 1]] = n - in;
                        else variables[(int) code[pointer + 1]] = in - n;
                    } catch (Exception e) {
                        throw new VMException("TSL", "error in var math");
                    }
                    pointer += 4;
                } //arfl

                case vecMathShift + 1 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    int c = (int) code[pointer + 3];

                    try {
                        b -= variables.length;
                        c -= variables.length;
                        variables[a] = vector2Var[b].x * vector2Var[c].x + vector2Var[b].y * vector2Var[c].y;
                    } catch (Exception e) {
                        try {
                            b -= vector2Var.length;
                            c -= vector2Var.length;
                            variables[a] = vector3Var[b].x * vector3Var[c].x
                                    + vector3Var[b].y * vector3Var[c].y
                                    + vector3Var[b].z * vector3Var[c].z;
                        } catch (Exception ex) {
                            throw new VMException("TSL", "Error in vec math");
                        }
                    }
                    pointer += 4;
                } //dot
                case vecMathShift + 2 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    int c = (int) code[pointer + 3];

                    try {
                        b -= variables.length;
                        c -= variables.length;
                        variables[a] = vector2Var[b].x * vector2Var[c].y - vector2Var[b].y * vector2Var[c].x;
                    } catch (Exception e) {
                        try {
                            a -= variables.length + vector2Var.length;
                            b -= vector2Var.length;
                            c -= vector2Var.length;
                            vector3Var[a] = new Vector3(
                                    vector3Var[b].y * vector3Var[c].z - vector3Var[b].z * vector3Var[c].y,
                                    vector3Var[b].z * vector3Var[c].x - vector3Var[b].x * vector3Var[c].z,
                                    vector3Var[b].x * vector3Var[c].y - vector3Var[b].y * vector3Var[c].x);
                        } catch (Exception ex) {
                            throw new VMException("TSL", "Error in vec math");
                        }
                    }
                    pointer += 4;
                } //crs
                case vecMathShift + 3 -> {
                    int a = (int) code[pointer + 1];
                    int b = (int) code[pointer + 2];
                    if (a < variables.length) {
                        try {
                            Vector2 v2 = vector2Var[b - variables.length];
                            variables[a] = (float) Math.sqrt(v2.x * v2.x + v2.y * v2.y);
                        } catch (Exception e) {
                            try {
                                Vector3 v3 = vector3Var[b - variables.length - vector2Var.length];
                                variables[a] = (float) Math.sqrt(v3.x * v3.x + v3.y * v3.y + v3.z * v3.z);
                            } catch (Exception ex) {
                                throw new VMException("TSL", "incorrect data");
                            }
                        }
                    } else throw new VMException("TSL", "error in vec math");
                    pointer += 3;
                } //vlen
                case vecMathShift + 4 -> {
                    try {
                        Vector2 p11 = vector2Var[(int) code[pointer + 2] - variables.length];
                        Vector2 p12 = vector2Var[(int) code[pointer + 3] - variables.length];
                        Vector2 p21 = vector2Var[(int) code[pointer + 4] - variables.length];
                        Vector2 p22 = vector2Var[(int) code[pointer + 5] - variables.length];

                        Vector2 cut1 = new Vector2(p12.x - p11.x, p12.y - p11.y);
                        Vector2 cut2 = new Vector2(p22.x - p21.x, p22.y - p21.y);

                        Vector2 d1 = new Vector2(p21.x - p11.x, p21.y - p11.y);
                        Vector2 d2 = new Vector2(p22.x - p11.x, p22.y - p11.y);

                        float prod1 = cut1.x * d1.y - cut1.y * d1.x;
                        float prod2 = cut1.x * d2.y - cut1.y * d2.x;


                        if (Math.signum(prod1) == Math.signum(prod2) || (prod1 == 0) || (prod2 == 0))
                            vector2Var[(int) code[pointer + 1] - variables.length] = new Vector2(0, 0);
                        else {

                            d2 = new Vector2(p12.x - p21.x, p12.y - p21.y);

                            prod1 = cut2.y * d1.x - cut2.x * d1.y;
                            prod2 = cut2.x * d2.y - cut2.y * d2.x;

                            if (Math.signum(prod1) == Math.signum(prod2) || (prod1 == 0) || (prod2 == 0))
                                vector2Var[(int) code[pointer + 1] - variables.length] = new Vector2(0, 0);
                            else vector2Var[(int) code[pointer + 1] - variables.length] = new Vector2(
                                    p11.x + cut1.x * Math.abs(prod1) / Math.abs(prod2 - prod1),
                                    p11.y + cut1.y * Math.abs(prod1) / Math.abs(prod2 - prod1));
                        }
                    } catch (Exception e) {
                        throw new VMException("TSL", "Error in vec2 math");
                    }
                    pointer += 6;
                } //itct
                case vecMathShift + 5 -> {
                    try {
                        Vector2 in = vector2Var[(int) code[pointer + 2] - variables.length];
                        Vector2 n = vector2Var[(int) code[pointer + 3] - variables.length];

                        float nl = (float) (2 * (in.x * n.x + in.y * n.y) / Math.sqrt(n.x * n.x + n.y * n.y));
                        vector2Var[(int) code[pointer + 1] - variables.length] = new Vector2(
                                in.x - n.x * nl,in.y - n.y * nl
                        );
                    } catch (Exception e) {
                        try {
                            Vector3 in = vector3Var[(int) code[pointer + 2] - variables.length - vector2Var.length];
                            Vector3 n = vector3Var[(int) code[pointer + 3] - variables.length - vector2Var.length];

                            float nl = (float) (2 * (in.x * n.x + in.y * n.y + in.z * n.z) / Math.sqrt(n.x * n.x + n.y * n.y + n.z * n.z));
                            vector3Var[(int) code[pointer + 1] - variables.length - vector2Var.length] = new Vector3(
                                    in.x - n.x * nl,in.y - n.y * nl,in.z - n.z * nl
                            );
                        } catch (Exception ex) {
                            throw new VMException("TSL", "Error in vec math");
                        }
                    }
                    pointer += 6;
                } //vrfl
                case vecMathShift + 6 -> {
                    int out = (int) code[pointer + 1];
                    try {
                        Vector2 pt = vector2Var[(int) code[pointer + 2] - variables.length];
                        Vector2 a = vector2Var[(int) code[pointer + 3] - variables.length];
                        Vector2 b = vector2Var[(int) code[pointer + 4] - variables.length];

                        float aptDst = (float) Math.sqrt(Math.pow(pt.x - a.x, 2) + Math.pow(pt.y - a.y, 2));
                        float bptDst = (float) Math.sqrt(Math.pow(pt.x - b.x, 2) + Math.pow(pt.y - b.y, 2));
                        float abDst = (float) Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));

                        if ((aptDst + bptDst - abDst) < 0.0001) variables[out] = 1;
                        else variables[out] = 0;
                    } catch (Exception e) {
                        throw new VMException("TSL", "Point in cut testing error");
                    }
                    pointer += 5;
                } //pinc
                case vecMathShift + 7 -> {
                    int a = (int) code[pointer + 1] - variables.length;
                    int b = (int) code[pointer + 2];
                    int c = (int) code[pointer + 3];

                    try {
                     vector2Var[a] = new Vector2(
                             (float) (Math.cos(variables[b]) * variables[c]),
                             (float) (Math.sin(variables[b]) * variables[c]));
                    } catch (Exception e) {
                        throw new VMException("TSL", "Error in ray creating");
                    }
                    pointer += 4;
                } //cray
                case vecMathShift + 8 -> {
                    try {
                        Vector2 a = vector2Var[(int) code[pointer + 2] - variables.length];
                        Vector2 b = vector2Var[(int) code[pointer + 3] - variables.length];
                        vector2Var[(int) code[pointer + 1] - variables.length] = new Vector2(b.y - a.y, a.x - b.x);
                    } catch (Exception e) {
                        throw new VMException("TSL", "Error in normal calculating");
                    }
                    pointer += 4;
                } //getn
                case vecMathShift + 9 -> {
                    int out = (int) code[pointer + 1] - variables.length;
                    if (out < vector2Var.length) {
                        try {
                            Vector2 a = vector2Var[(int) code[pointer + 2] - variables.length];
                            Vector2 b = vector2Var[(int) code[pointer + 3] - variables.length];
                            float t = variables[(int) code[pointer + 4]];

                            vector2Var[out] = new Vector2(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t);
                        } catch (Exception e) {
                            throw new VMException("TSL", "Vec2 lerp error");
                        }
                    }
                    else {
                        try {
                            Vector3 a = vector3Var[(int) code[pointer + 2] - variables.length - vector2Var.length];
                            Vector3 b = vector3Var[(int) code[pointer + 3] - variables.length - vector2Var.length];
                            float t = variables[(int) code[pointer + 4]];

                            vector3Var[out - vector2Var.length] = new Vector3(
                                    a.x + (b.x - a.x) * t,
                                    a.y + (b.y - a.y) * t,
                                    a.z + (b.z - a.z) * t);
                        } catch (Exception e) {
                            throw new VMException("TSL", "Vec3 lerp error");
                        }
                    }
                    pointer += 5;
                } //lerp

                case funcShift + 1 -> {
                    return pointer;
                } //end
                case funcShift + 2 -> {
                    int a = (int) code[pointer + 1];
                    if (variables[a] > 0.5) {
                        pointer = runner(code, pointer + 2);
                    }
                    pointer++;
                } //if
                case funcShift + 3 -> {
                    int a = (int) code[pointer + 1];
                    int start = pointer + 2;
                    while (variables[a] > 0.5) {
                        pointer = runner(code, start);
                    }
                    pointer++;
                } //cyc
                case funcShift + 4 -> throw new VMException("TSL", (int) variables[(int) code[pointer + 1]]); //kill
            }
        }
        return pointer;
    }

    public void setVariable(int i, Object var) {
        if (i >= variables.length) {
            i -= variables.length;
            if (i >= vector2Var.length) {
                i -= vector2Var.length;
                if (i >= vector3Var.length) throw new VMException("TSL", "Memory heap overflow");
                else vector3Var[i] = (Vector3) var;
            }
            else vector2Var[i] = (Vector2) var;
        }
        else variables[i] = (float) var;
    }
    public Object getVariable(int i) {
        if (i >= variables.length) {
            i -= variables.length;
            if (i >= vector2Var.length) {
                i -= vector2Var.length;
                if (i >= vector3Var.length) throw new VMException("TSL", "Memory heap overflow");
                else return vector3Var[i];
            }
            else return vector2Var[i];
        }
        else return variables[i];
    }


    public static class Vector2 {
        public float x;
        public float y;

        public Vector2(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Vector2{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
    public static class Vector3 {
        public float x;
        public float y;
        public float z;

        public Vector3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            return "Vector3{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }
    }
}
