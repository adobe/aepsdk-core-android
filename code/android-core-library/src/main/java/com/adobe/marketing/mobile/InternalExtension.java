package com.adobe.marketing.mobile;


/**
 * Provide additional helper methods to easily port internal modules as third party extensions.
 */
abstract class InternalExtension extends Extension {

    /**
     * Construct the extension and initialize with the {@code ExtensionApi}.
     *
     * @param extensionApi the {@link ExtensionApi} this extension will use
     */
    protected InternalExtension(ExtensionApi extensionApi) {
        super(extensionApi);
    }


}
