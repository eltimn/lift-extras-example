"use strict";

const KarmaServer = require("karma").Server,
      argv = require("yargs").argv,
      cleanCSS = require("gulp-clean-css"),
      concat = require("gulp-concat"),
      debug = require("gulp-debug"),
      del = require("del"),
      filter = require("gulp-filter"),
      gulp = require("gulp"),
      gulpif = require("gulp-if"),
      gzip = require("gulp-gzip"),
      hash = require("gulp-hash"),
      jshint = require("gulp-jshint"),
      less = require("gulp-less"),
      pkg = require("./package.json"),
      pump = require("pump"),
      runSequence = require("run-sequence"),
      streamqueue = require("streamqueue"),
      uglify = require("gulp-uglify");

const config = require("./build.config");
const distDir = config.dirs.target + "/dist";
const resourcesDir = config.dirs.target + "/resources";

const browserSync = require("browser-sync").create();

function reload() {
  browserSync.reload();
}

var settings = {
  watchInterval: 200
};

try {
  settings = require("./localsettings.json");
  console.log("Reading ./localsettings.json", settings);
} catch (ignore) {
  console.log("./localsettings.json not found, using defaults");
}

function jsGroupTask(group) {

  var isJsHintEnabled = typeof group.jshint === "string";
  const filterNonMinified = filter(["**", "!**/*.min.js"], {restore: true});

  return gulp.src(group.files)
    .pipe(gulpif(isJsHintEnabled, jshint(group.jshint)))
    .pipe(gulpif(isJsHintEnabled, jshint.reporter("jshint-stylish")))
    .pipe(gulpif(argv.dist && isJsHintEnabled, jshint.reporter("fail")))
    .pipe(gulpif(argv.dist, filterNonMinified))
    .pipe(gulpif(argv.dist, uglify()))
    .pipe(gulpif(argv.dist, filterNonMinified.restore));
  ;
}


function jsTask(name) {
  const bundle = config.bundles.js[name];
  var args = bundle.map(function(group) {
    return jsGroupTask(group);
  });

  args.unshift({ objectMode: true });

  return [
    streamqueue.apply(this, args),
    concat(name + ".js"),
    gulpif(argv.dist, hash()),
    gulp.dest(distDir + "/js"),
    gulpif(argv.dist, hash.manifest("js.manifest.json", true)),
    gulpif(argv.dist, gulp.dest(resourcesDir))
  ];
}

function lessTask(name) {
  const bundle = config.bundles.less[name];
  const filterNonMinified = filter(["**", "!**/*.min.css"], {restore: true});

  var lessOpts = bundle.options || {};

  var lessPipeline =
    gulp.src(bundle.source)
      .pipe(less(lessOpts))
      .pipe(gulpif(argv.dist, cleanCSS()))

  var depsPipeline =
    gulp.src(bundle.deps)
      .pipe(gulpif(argv.dist, filterNonMinified))
      .pipe(gulpif(argv.dist, cleanCSS()))
      .pipe(gulpif(argv.dist, filterNonMinified.restore))

  return [
    streamqueue({ objectMode: true },
      depsPipeline,
      lessPipeline
    ),
    concat(name + ".css"),
    gulpif(argv.dist, hash()),
    gulp.dest(distDir + "/css"),
    browserSync.stream(),
    gulpif(argv.dist, hash.manifest("css.manifest.json", true)),
    gulpif(argv.dist, gulp.dest(resourcesDir))
  ];
}

function copyTask(name, cb) {
  const bundle = config.bundles.copy[name];

  return [
    gulp.src(bundle.files, { base: bundle.base }),
    gulp.dest(distDir)
  ];
}

// tasks
gulp.task("default", ["build"], (callback) => {
  runSequence("test", callback);
});

gulp.task("build", ["js", "less", "copy"]);

gulp.task("dist", ["test"], (callback) => {
  runSequence(
    "clean",
    "build",
    "gzip",
    callback);
});

gulp.task("js", Object.keys(config.bundles.js).map((bundle) => "js:"+bundle));
Object.keys(config.bundles.js).forEach((bundle) => {
  gulp.task("js:"+bundle, function(cb) {
    pump(jsTask(bundle), cb);
  });
})

gulp.task("less", Object.keys(config.bundles.less).map((bundle) => "less:"+bundle));
Object.keys(config.bundles.less).forEach((bundle) => {
  gulp.task("less:"+bundle, function(cb) {
    pump(lessTask(bundle), cb);
  });
})

gulp.task("copy", Object.keys(config.bundles.copy).map((bundle) => "copy:"+bundle));
Object.keys(config.bundles.copy).forEach((bundle) => {
  gulp.task("copy:"+bundle, function(cb) {
    pump(copyTask(bundle), cb);
  });
})

// gzip
gulp.task("gzip", function(cb) {
  pump([
    gulp.src([distDir+"/css/*.css", distDir+"/js/*.js"], { base: distDir }),
    gzip(),
    gulp.dest(distDir)
  ], cb);
});

// test
gulp.task("test", function(cb) {
  let files = Object.keys(config.bundles.js)
    .map(function(name) {
      let bundle = config.bundles.js[name];
      return bundle
        .map(function(bun) {
          return bun.files;
        })
        .reduce(function(a, b) {
          return a.concat(b);
        });
    })
    .reduce(function(a, b) {
      return a.concat(b);
    });

  let conf = {
    configFile: __dirname + "/karma.conf.js",
    files: files.concat([
      config.dirs.tests + "/js/**/*[sS]pec.js"
    ]),
    singleRun: true,
    port: 9877
  };
  new KarmaServer(conf, cb).start();
});

// clean
gulp.task("clean", () => {
  return del([config.dirs.target]);
});

gulp.task("clean-deps", () => {
  return del(config.dirs.bower_components);
});


// watch
Object.keys(config.bundles.js).forEach((bundle) => {
  gulp.task("js:watch:"+bundle, ["js:"+bundle], reload);
})

gulp.task("watch", ["build"], function() {

  browserSync.init(config.browserSyncOpts);

  // watch each js bundle and trigger independently
  Object.keys(config.bundles.js).forEach((name) => {
    const bundle = config.bundles.js[name];

    bundle.forEach((group) => {
      if (typeof group.watch === "boolean" && group.watch) {
        gulp.watch(group.files, {interval: settings.watchInterval}, ["js:watch:"+name]);
      }
    });
  });

  gulp.watch(config.dirs.tests + "/js/**/*[sS]pec.js", {interval: settings.watchInterval}, ["test"]);
  gulp.watch(config.dirs.assets + "/less/**/*.less", {interval: settings.watchInterval}, ["less"]);
  gulp.watch(config.watch_files, {interval: settings.watchInterval}).on("change", reload);
  gulp.watch("target/bsync.reload", {interval: settings.watchInterval}).on("change", reload);
});
