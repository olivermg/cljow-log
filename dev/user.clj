(ns user
  (:require [cljs.repl.node :as rn]
            [figwheel-sidecar.repl-api :as f]))

(defn start-figwheel []
  (f/start-figwheel! "nodejs-dev"))

(defn stop-figwheel []
  (f/stop-figwheel!))

(defn node-repl []
  ;;; NOTE: for a prompt to appear in a figwheel repl, a client needs to connect, i.e.
  ;;;   web: a browser loading the compiled js code
  ;;;   node: a node process loading the compiled js code
  (f/cljs-repl "nodejs-dev"))
