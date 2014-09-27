(defproject daemonistic "0.2.0-SNAPSHOT"
  :description "OOCSI client API for Clojure using core.async"
  :url "https://github.com/sander/daemonistic"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [oocsi "0.5.0-SNAPSHOT"]]
  :plugins [[cider/cider-nrepl "0.8.0-SNAPSHOT"]]
  :eval-in :nrepl
  :scm {:name "git"
        :url "https://github.com/sander/daemonistic"})
