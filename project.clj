(defproject ta-crash "0.1.0-SNAPSHOT"
  :description "Transportation Altertnatives Crash Stats"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.omcljs/om "1.0.0-alpha28"]
                 [org.clojure/core.async "0.2.371" :scope "test"]
                 [figwheel-sidecar "0.5.0-SNAPSHOT" :scope "test"]]
  :plugins [[lein-cljsbuild "1.1.2"]]
  :cljsbuild {:builds {:app {:source-paths ["src/ta_crash"]
                             :compiler {:output-to     "dist/public/js/app.js"
                                        :output-dir    "dist/public/js/out"
                                        :source-map    "dist/public/js/out.js.map"
                                        :preamble      ["react/react.min.js"]
                                        :optimizations :whitespace
                                        :pretty-print  false}}}})
