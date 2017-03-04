class Utils {
    static resolveVersion() {
        def branch  = env('TRAVIS_BRANCH')?.replace('/', '_')?.toLowerCase()
        def version = (branch in ['master', null] ? 'latest' : branch)

        if (env('TRAVIS_TAG')) { version = env('TRAVIS_TAG') }

        return version
    }

    private static env(String name, String defaultValue = null) {
        System.getenv(name) ?: defaultValue
    }
}
