function(doc) {

	if( doc 
	 && doc.type === 'user'
	 && doc.nunaliit_validated_emails
	 && doc.nunaliit_validated_emails.length
		 ) {
		// Loop through roles looking for vetter
		for(var i=0,e=doc.nunaliit_validated_emails.length;i<e;++i){
			var email = doc.nunaliit_validated_emails[i];
			emit(email,null);
		};
	};
}