/**
  * This file/module contains all configuration for the build process.
  */
"use strict";

let dirs = {
  "assets": __dirname + "/src/main/assets",
  "tests": __dirname + "/src/test/assets",
  "target": __dirname + "/target/frontend",
  "bower_components": __dirname + "/bower_components",
  "webjars": __dirname + "/target/webjars"
};

module.exports = {
  "dirs": dirs,
  "browserSyncOpts": {
    "snippetOptions": {
      // Provide a custom Regex for inserting the snippet.
      "rule": {
        "match": /<\/body>/i,
        "fn": function (snippet, match) {
          return ""; // don't inject. Use BrowserSync snippet instead.
        }
      }
    },
    "proxy": {
      "target": "localhost:8080"
    },
    "open": false,
    "ghostMode": false
  },
  "watch_files": "src/main/webapp/**/*.html",
  "bundles": {
    "js": {
      "main": [
        {
          "files": [
            dirs.webjars + "/jquery/dist/jquery.min.js",
            dirs.webjars + "/bootstrap/dist/js/bootstrap.min.js",
            dirs.bower_components + "/angular/angular.min.js",
            dirs.bower_components + "/knockout/dist/knockout.js",
            dirs.webjars + "/lift-extras/jquery.bsAlerts.min.js",
            dirs.webjars + "/lift-extras/jquery.bsFormAlerts.min.js"
          ]
        },
        {
          "files": [
            dirs.assets + "/js/App.js",
            dirs.assets + "/js/KoAlerts.js",
            dirs.assets + "/js/main/apps/**/*.js",
            dirs.assets + "/js/main/utils/**/*.js",
            dirs.assets + "/js/main/views/**/*.js"
          ],
          "jshint": ".jshintrc",
          "watch": true
        }
      ]
    },
    "less": {
      "main": {
        "source": dirs.assets + "/less/styles.less",
        "options": {
          "paths": [
            dirs.webjars + "/bootstrap/less",
            dirs.assets + "/vendor/"
          ]
        },
        // other css files that will be prepended to bundle
        "deps": [
          dirs.assets +"/vendor/red.css"
        ]
      }
    },
    "copy": {
      "bsfonts": {
        "base": dirs.webjars + "/bootstrap",
        "files": [
          dirs.webjars + "/bootstrap/fonts/*"
        ]
      }
    }
  }
};
