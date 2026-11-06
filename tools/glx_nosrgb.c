/*
 * Test-only OpenGL shim for tools/devtest.sh. Minecraft 26.3 (and NeoForge's early loading screen) open their window
 * with SDL3 and ask for an sRGB-capable framebuffer, which Xvfb + Mesa llvmpipe do not offer ("Couldn't find matching
 * GLX visual"). devtest.sh builds this into a small library that wraps libGL.so.1 and points SDL_OPENGL_LIBRARY at it;
 * it drops the sRGB request from GLX visual/config queries. The game's colours are unaffected under llvmpipe.
 */
#define _GNU_SOURCE
#include <dlfcn.h>
#include <stddef.h>
#include <string.h>

#define GLX_FRAMEBUFFER_SRGB_CAPABLE 0x20B2
#define END 0

typedef void *(*choose_fb_t)(void *, int, const int *, int *);
typedef void *(*choose_vis_t)(void *, int, int *);
typedef void *(*gpa_t)(const unsigned char *);

static void *real(const char *name) {
    static void *gl;
    if (!gl) gl = dlopen("libGL.so.1", RTLD_NOW | RTLD_GLOBAL);
    return dlsym(gl, name);
}

/* glXChooseFBConfig lists are (key, value) pairs. */
void *glXChooseFBConfig(void *dpy, int screen, const int *attribs, int *n) {
    int out[256];
    int j = 0;
    for (int i = 0; attribs && attribs[i] != END && j < 254; i += 2) {
        if (attribs[i] == GLX_FRAMEBUFFER_SRGB_CAPABLE) continue;
        out[j++] = attribs[i];
        out[j++] = attribs[i + 1];
    }
    out[j] = END;
    return ((choose_fb_t) real("glXChooseFBConfig"))(dpy, screen, attribs ? out : NULL, n);
}

/* glXChooseVisual lists mix single boolean tokens (GLX_USE_GL, GLX_RGBA, GLX_DOUBLEBUFFER, GLX_STEREO) with pairs. */
void *glXChooseVisual(void *dpy, int screen, int *attribs) {
    int out[256];
    int j = 0;
    for (int i = 0; attribs && attribs[i] != END && j < 254;) {
        int key = attribs[i];
        int single = key == 1 || key == 4 || key == 5 || key == 6;
        if (key != GLX_FRAMEBUFFER_SRGB_CAPABLE) {
            out[j++] = key;
            if (!single) out[j++] = attribs[i + 1];
        }
        i += single ? 1 : 2;
    }
    out[j] = END;
    return ((choose_vis_t) real("glXChooseVisual"))(dpy, screen, attribs ? out : NULL);
}

/* SDL looks the GLX entry points up through glXGetProcAddress, so hand out the filtered ones there too. */
static void *lookup(const char *gpa, const unsigned char *name) {
    if (strcmp((const char *) name, "glXChooseFBConfig") == 0) return (void *) glXChooseFBConfig;
    if (strcmp((const char *) name, "glXChooseVisual") == 0) return (void *) glXChooseVisual;
    return ((gpa_t) real(gpa))(name);
}

void *glXGetProcAddressARB(const unsigned char *name) {
    return lookup("glXGetProcAddressARB", name);
}

void *glXGetProcAddress(const unsigned char *name) {
    return lookup("glXGetProcAddress", name);
}
