// Plugins
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import Fonts from "unplugin-fonts/vite";
import Layouts from "vite-plugin-vue-layouts-next";
import Vue from "@vitejs/plugin-vue";
import VueRouter from "unplugin-vue-router/vite";
import { VueRouterAutoImports } from "unplugin-vue-router";
import Vuetify, { transformAssetUrls } from "vite-plugin-vuetify";

// Utilities
import { defineConfig, loadEnv } from "vite";
import { fileURLToPath, URL } from "node:url";

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd());
  const isTest = mode === "test";
  return {
    base: mode === "production" ? "/paw-2024a-07" : "/",
    plugins: [
      VueRouter({
        dts: "src/typed-router.d.ts",
      }),
      Layouts(),
      AutoImport({
        imports: [
          "vue",
          VueRouterAutoImports,
          {
            pinia: ["defineStore", "storeToRefs"],
          },
        ],
        dts: "src/auto-imports.d.ts",
        eslintrc: {
          enabled: true,
        },
        vueTemplate: true,
      }),
      Components({
        dts: "src/components.d.ts",
      }),
      Vue({
        template: { transformAssetUrls },
      }),
      // https://github.com/vuetifyjs/vuetify-loader/tree/master/packages/vite-plugin#readme
      Vuetify({
        autoImport: true,
        styles: {
          configFile: "src/styles/settings.scss",
        },
      }),
      Fonts({
        fontsource: {
          families: [
            {
              name: "Roboto",
              weights: [400, 500, 700],
              styles: ["normal", "italic"],
              subset: "latin-ext",
            },
          ],
        },
      }),
    ],
    optimizeDeps: {
      exclude: [
        "vuetify",
        "vue-router",
        "unplugin-vue-router/runtime",
        "unplugin-vue-router/data-loaders",
        "unplugin-vue-router/data-loaders/basic",
      ],
    },
    define: { "process.env": {} },
    resolve: {
      alias: [
        {
          find: "@",
          replacement: fileURLToPath(new URL("src", import.meta.url)),
        },
        ...(isTest
          ? [
              {
                find: /vuetify[\\/]{1}lib[\\/]{1}components[\\/].*\.css$/,
                replacement: fileURLToPath(new URL("src/test/styleStub.ts", import.meta.url)),
              },
            ]
          : []),
      ],
      extensions: [".js", ".json", ".jsx", ".mjs", ".ts", ".tsx", ".vue"],
    },
    server: {
      port: 3000,
      proxy: {
        "/api": {
          target: env.VITE_API_BASE_URL,
          changeOrigin: true,
        },
      },
    },
    css: {
      preprocessorOptions: {
        sass: {
          api: "modern-compiler",
        },
        scss: {
          api: "modern-compiler",
        },
      },
    },
    test: {
      globals: true,
      environment: "jsdom",
      setupFiles: "src/test/setup.ts",
      deps: {
        inline: ["vuetify"],
      },
      css: true,
      coverage: {
        reporter: ["text", "html"],
        reportsDirectory: "coverage",
      },
    },
  };
});
