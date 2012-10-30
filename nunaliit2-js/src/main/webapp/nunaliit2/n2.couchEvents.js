/*
Copyright (c) 2012, Geomatics and Cartographic Research Centre, Carleton 
University
All rights reserved.

Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, 
   this list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 - Neither the name of the Geomatics and Cartographic Research Centre, 
   Carleton University nor the names of its contributors may be used to 
   endorse or promote products derived from this software without specific 
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

$Id$
*/
;(function($,$n2){

// Localization
var _loc = function(str){ return $n2.loc(str,'nunaliit2-couch'); };
var DH = 'n2.couchEvents';

var EventSupport = $n2.Class('EventSupport',{
	options: null

	,dispatchCallback: null
	
	,handler: null
	
	,initialize: function(opts_){
		this.options = $n2.extend({
			directory: null
		},opts_);
		
		var _this = this;
		
		this.handler = function(m){
			_this._defaultHandler(m);
		};
		
		this.dispatchCallback = function(m){
			_this.handler(m);
		};
		
		this.register('userSelect');
	}

	,register: function(type){
		var d = this._getDispatcher();
		if( d ){
			d.register(DH,type,this.dispatchCallback);
		};
	}
	
	,setHandler: function(handler){
		if( typeof(handler) === 'function' ){
			this.handler = handler;
		};
	}
	
	,_getDispatcher: function(){
		var d = null;
		if( this.options.directory ){
			d = this.options.directory.dispatchService;
		};
		return d;
	}
	
	,_dispatch: function(m){
		var d = this._getDispatcher();
		if( d ){
			d.send(DH,m);
		};
	}

	,_defaultHandler: function(m){
		if( 'userSelect' === m.type ) {
			this._dispatch({
				type:'selected'
				,docId: m.docId
				,doc: m.doc
				,feature: m.feature
			});
		};
	}
});

$n2.couchEvents = {
	EventSupport: EventSupport
};

})(jQuery,nunaliit2);
