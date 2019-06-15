(ns ow.logging.macros)

;;; NOTE: macros always get compiled in java/jvm, even for cljs code, because they are compile time citizens.
;;;   this implies that we need to define them in .clj or .cljc files, not in .cljs files.
;;;   and we need to require them via :require-macros in cljs code.

(defmacro with-trace* [name [& args] & body]
  `(binding [ow.logging/+callinfo+ (update ow.logging/+callinfo+ :trace conj (ow.logging/make-trace-info* '~name ~@args))]
     ~@body))

(defmacro with-trace
  "Adds an entry into the current trace history."
  [name & body]
  `(with-trace* ~name []
     ~@body))

(defmacro with-trace-data
  "Adds user data into the current trace info map that will be available in subsequent log invocations."
  [data & body]
  `(binding [ow.logging/+callinfo+ (update ow.logging/+callinfo+ :data merge (pr-str-map-vals ~data))]
     ~@body))
