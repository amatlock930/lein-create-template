(ns leiningen.create-template-spec
  (:use speclj.core
        leiningen.create-template)
  (:require [clojure.java.io :as jio]
            [clojure.string :as cs]))

(def root-path "/home/user/leiningen/tempalte/")

(defn mock-file [relative-file-name]
  (jio/as-file (str root-path relative-file-name)))

(def source-file (mock-file "/src/rest_ful.clj"))
(def resource-file (mock-file "/resources/index-site.html"))

(def mock-info
  {:root-path root-path
   :old-project-name "rest-ful"
   :new-project-name "rest-ful-template"
   :project-file (mock-file "/project.clj")
   :source-files [source-file (mock-file "/src/rest_ful_2.clj")]
   :resource-files [resource-file (mock-file "/resources/some_crazy-File.js")]
   :test-source-files [(mock-file "/src/rest_ful_test.clj") (mock-file "/src/rest_ful_2_test.clj")]
   :test-resource-files [(mock-file "/resources/index-site.html") (mock-file "/resources/some_crazy-File.js")]})



(describe "File and namespace utils"

  (it "sanitizes a clojure namespace to file name"
    (should= "create_template" (sanitize-from-clj "create-template"))
    (should= "create_template_more" (sanitize-from-clj "create-template-more"))
    (should= "createless" (sanitize-from-clj "createless"))
    (should= "create_less" (sanitize-from-clj "create_less")))

  (it "sanitizes a file name to clojure namespace"
    (should= "create-template" (sanitize-to-clj "create_template"))
    (should= "create-template-more" (sanitize-to-clj "create_template_more"))
    (should= "createless" (sanitize-to-clj "createless"))
    (should= "create-less" (sanitize-to-clj "create-less")))

  (it "sanitizes a project name to lein-newnew template form"
    (should= "leiningen.new.{{sanitized}}" (sanitize-project-name "leiningen.new.mytemplate" "mytemplate"))
    (should= "leiningen.new.{{sanitized}}.other" (sanitize-project-name "leiningen.new.mytemplate.other" "mytemplate"))
    (should= "{{sanitized}}" (sanitize-project-name "mytemplate" "mytemplate")))

  (it "creates a relative file path for a file from an absolute root path"
    (should= "lein/template.clj" (relative-path (jio/as-file "home/user/lein/template.clj") "home/user"))
    (should-not= "lein/template.clj" (relative-path (jio/as-file "home/user/lein/template.clj") "home/use")))

  (it "creates a relative path for files in the new lein template project"
    (should= "home/user/mytemplates/ringtemplate/src/leiningen/new/ringtemplate/" (new-lein-path "home/user/mytemplates" "ringtemplate")))

  (it "creates a new file containing the new lein template filepath"
    (should= "/home/user/leiningen/tempalte/ringtemplate/src/leiningen/new/ringtemplate/rest_ful.clj" (str (get-new-sanitized-lein-file source-file root-path "ringtemplate")))
    )
  )

(describe "Generating clj code files"

  (it "generates a new clj code line that points to the file"
    (should= "[\"/home/user/leiningen/tempalte/src/rest_ful.clj\" (render \"rest_ful.clj\" data)]" (make-file-line source-file root-path (:new-project-name mock-info) true))
    (should= "[\"/home/user/leiningen/tempalte/resources/index-site.html\" (render \"index_site.html\")]" (make-file-line resource-file root-path (:new-project-name mock-info) false)))

  (it "generates a new template clj code file that specifies how to render resources"
    (should (re-seq #"\[\"/home/user/leiningen/tempalte/resources/index-site\.html"
              (create-template-render-file (:source-files mock-info) (:resource-files mock-info) mock-info))))

  (it "generates a new template clj project file"
    (let [template (create-project-template-file mock-info)]
      (should (re-seq #"ct rest-ful-template/lein-t" template))
      (should (re-seq #"eval-in-leiningen true\)" template))))

  )

(run-specs)