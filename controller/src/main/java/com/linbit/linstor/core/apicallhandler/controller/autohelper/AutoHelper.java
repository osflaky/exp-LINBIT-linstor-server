package com.linbit.linstor.core.apicallhandler.controller.autohelper;


interface AutoHelper
{
    void manage(AutoHelperContext ctx);
    AutoHelperType getType();
}
