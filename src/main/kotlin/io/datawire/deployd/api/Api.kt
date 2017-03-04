package io.datawire.deployd.api

import io.vertx.ext.web.Router


interface Api {

    /**
     * Register additional routes with the provided router.
     *
     * @param router the router to configure with additional routes.
     */
    fun configure(router: Router)
}