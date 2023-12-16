package org.java2.backend.constant;

public enum FatalErrors {
    AWT_ERROR("AWTError"),
    IO_ERROR("IOError"),
    ABSTRACT_METHOD_ERROR("AbstractMethodError"),
    ASSERTION_ERROR("AssertionError"),
    BOOTSTRAP_METHOD_ERROR("BootstrapMethodError"),
    CLASS_CIRCULARITY_ERROR("ClassCircularityError"),
    CLASS_FORMAT_ERROR("ClassFormatError"),
    EXCEPTION_IN_INITIALIZER_ERROR("ExceptionInInitializerError"),
    ILLEGAL_ACCESS_ERROR("IllegalAccessError"),
    INCOMPATIBLE_CLASS_CHANGE_ERROR("IncompatibleClassChangeError"),
    INSTANTIATION_ERROR("InstantiationError"),
    INTERNAL_ERROR("InternalError"),
    LINKAGE_ERROR("LinkageError"),
    NO_CLASS_DEF_FOUND_ERROR("NoClassDefFoundError"),
    NO_SUCH_FIELD_ERROR("NoSuchFieldError"),
    NO_SUCH_METHOD_ERROR("NoSuchMethodError"),
    OUT_OF_MEMORY_ERROR("OutOfMemoryError"),
    STACK_OVERFLOW_ERROR("StackOverflowError"),
    THREAD_DEATH("ThreadDeath"),
    UNKNOWN_ERROR("UnknownError"),
    UNSATISFIED_LINK_ERROR("UnsatisfiedLinkError"),
    UNSUPPORTED_CLASS_VERSION_ERROR("UnsupportedClassVersionError"),
    VERIFY_ERROR("VerifyError"),
    VIRTUAL_MACHINE_ERROR("VirtualMachineError"),
    ANNOTATION_FORMAT_ERROR("AnnotationFormatError"),
    GENERIC_SIGNATURE_FORMAT_ERROR("GenericSignatureFormatError"),
    CODER_MALFUNCTION_ERROR("CoderMalfunctionError"),
    SERVER_ERROR("ServerError"),
    SERVICE_CONFIGURATION_ERROR("ServiceConfigurationError"),
    ZIP_ERROR("ZipError"),
    FACTORY_CONFIGURATION_ERROR("FactoryConfigurationError"),
    TRANSFORMER_FACTORY_CONFIGURATION_ERROR("TransformerFactoryConfigurationError"),
    SCHEMA_FACTORY_CONFIGURATION_ERROR("SchemaFactoryConfigurationError"),
    PROVIDER_CONFIGURATION_ERROR("ProviderConfigurationError");


    private final String errorName;

    FatalErrors(String errorName) {
        this.errorName = errorName;
    }

    public String getErrorName() {
        return this.errorName;
    }
}
