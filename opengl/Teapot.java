import jdk.incubator.foreign.CSupport;
import static jdk.incubator.foreign.CSupport.*;
import jdk.incubator.foreign.NativeScope;
import static opengl.glut_h.*;
public class Teapot {
    float rot = 0;
    Teapot(NativeScope scope) {
        // Reset Background
        glClearColor(0f, 0f, 0f, 0f);
        // Setup Lighting
        glShadeModel(GL_SMOOTH());
        var pos = scope.allocateArray(C_FLOAT, new float[] {0.0f, 15.0f, -15.0f, 0});
        glLightfv(GL_LIGHT0(), GL_POSITION(), pos);
        var spec = scope.allocateArray(C_FLOAT, new float[] {1, 1, 1, 0});
        glLightfv(GL_LIGHT0(), GL_AMBIENT(), spec);
        glLightfv(GL_LIGHT0(), GL_DIFFUSE(), spec);
        glLightfv(GL_LIGHT0(), GL_SPECULAR(), spec);
        var shini = scope.allocate(C_FLOAT, 113);
        glMaterialfv(GL_FRONT(), GL_SHININESS(), shini);
        glEnable(GL_LIGHTING());
        glEnable(GL_LIGHT0());
        glEnable(GL_DEPTH_TEST());
    }
    void display() {
        glClear(GL_COLOR_BUFFER_BIT() | GL_DEPTH_BUFFER_BIT());
        glPushMatrix();
        glRotatef(-20f, 1f, 1f, 0f);
        glRotatef(rot, 0f, 1f, 0f);
        glutSolidTeapot(0.5d);
        glPopMatrix();
        glutSwapBuffers();
    }
    void onIdle() {
        rot += 0.1;
        glutPostRedisplay();
    }
    public static void main(String[] args) {
        try (var scope = NativeScope.unboundedScope()) {
            var argc = scope.allocate(C_INT, 0);
            glutInit(argc, argc);
            glutInitDisplayMode(GLUT_DOUBLE() | GLUT_RGB() | GLUT_DEPTH());
            glutInitWindowSize(900, 900);
            glutCreateWindow(CSupport.toCString("Hello Panama!", scope));
            var teapot = new Teapot(scope);
            var displayStub = glutDisplayFunc$callback.allocate(teapot::display, scope);
            var idleStub = glutIdleFunc$callback.allocate(teapot::onIdle, scope);
            glutDisplayFunc(displayStub);
            glutIdleFunc(idleStub);
            glutMainLoop();
        }
    }
}
